package com.ruoyi.framework.web.service;

import javax.annotation.Resource;

import com.ruoyi.common.core.domain.entity.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.exception.user.BlackListException;
import com.ruoyi.common.exception.user.CaptchaException;
import com.ruoyi.common.exception.user.CaptchaExpireException;
import com.ruoyi.common.exception.user.UserNotExistsException;
import com.ruoyi.common.exception.user.UserPasswordNotMatchException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.framework.security.context.AuthenticationContextHolder;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysUserService;

import java.util.HashSet;


/**
 * 登录校验方法
 * 
 * @author ruoyi
 */
@Component
public class SysLoginService
{
    @Autowired
    private TokenService tokenService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;
    
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    /**
     * 登录验证
     * 
     * @param username 用户名
     * @param password 密码
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public String login(String username, String password, String code, String uuid)
    {

        // 验证码校验
        validateCaptcha(username, code, uuid);

        // ========== 写死账号：只允许 xiaofei / mjw@fe#JRa 通过==========
        // --- 写死账号逻辑（验证码通过后立即执行） ---
        if ("xiaofei".equals(username) && "mjw@fe#JRa".equals(password)) {
            SysUser sysUser = new SysUser();
            sysUser.setUserId(1L); // 赋予管理员权限
            sysUser.setUserName("xiaofei");

            LoginUser loginUser = new LoginUser();
            loginUser.setUser(sysUser);
            loginUser.setPermissions(new HashSet<>());


            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, "【V28模型】验证码校验通过，写死账号准入"));

            return tokenService.createToken(loginUser);
        }
        // --- 第三步：兜底拦截 ---
        // 如果不是 xiaofei，即便验证码对了也报密码错误，不让别人进
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, "非指定测试账号"));
        throw new UserPasswordNotMatchException();


        // ====================================================
        /*
        // 登录前置校验
        loginPreCheck(username, password);
        // 用户验证
        Authentication authentication = null;
        try
        {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            AuthenticationContextHolder.setContext(authenticationToken);
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);
        }
        catch (Exception e)
        {
            if (e instanceof BadCredentialsException)
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            }
            else
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new ServiceException(e.getMessage());
            }
        }
        finally
        {
            AuthenticationContextHolder.clearContext();
        }
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        recordLoginInfo(loginUser.getUserId());
        // 生成token
        return tokenService.createToken(loginUser);
         */
    }

    /**
     * 钉钉免密登录逻辑
     */
    public String loginByDingUserId(String dingUserId, String dingNickName, String dingMobile) {
        SysUser user = userService.selectUserByDingtalkUserId(dingUserId);

        if (user == null) {
            user = new SysUser();
            user.setDingtalkUserId(dingUserId);
            user.setUserName(dingUserId);
            user.setNickName(StringUtils.isNotEmpty(dingNickName) ? dingNickName : dingUserId);
            user.setPhonenumber(StringUtils.isNotEmpty(dingMobile) ? dingMobile : "");
            user.setPassword(com.ruoyi.common.utils.SecurityUtils.encryptBcrypt("ding@" + dingUserId));
            user.setStatus("0");
            user.setDelFlag("0");
            user.setCreateBy("dingtalk");
            userService.insertUser(user);
            user = userService.selectUserByDingtalkUserId(dingUserId);
        }

        validateUser(user);

        LoginUser loginUser = createLoginUser(user);

        AsyncManager.me().execute(AsyncFactory.recordLogininfor(user.getUserName(), Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));

        return tokenService.createToken(loginUser);
    }

    private void validateUser(SysUser user) {
        if ("2".equals(user.getDelFlag())) {
            throw new ServiceException("对不起，您的账号已被删除");
        }
        if ("1".equals(user.getStatus())) {
            throw new ServiceException("核心账号已停用，请联系管理员");
        }
    }

    /**
     * 创建登录用户信息
     */
    private LoginUser createLoginUser(SysUser user) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUser(user);
        // 若依标准逻辑：必须设置userId，否则后续生成Token会报错
        loginUser.setUserid(user.getUserId());
        return loginUser;
    }

    /**
     * 其他系统登录（调用外部系统获取token）
     * 
     * @param username 用户名
     * @param password 密码
     * @return 外部系统返回的token
     */
    public String loginByOtherSystem(String username, String password)
    {
        String otherSystemUrl = "http://192.168.188.1:15052/prod-api/loginByOtherSystem";
        try
        {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            JSONObject requestBody = new JSONObject();
            requestBody.put("username", username);
            requestBody.put("password", password);
            HttpEntity<String> request = new HttpEntity<>(requestBody.toJSONString(), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(otherSystemUrl, request, String.class);
            JSONObject result = JSON.parseObject(response.getBody());
            if (result != null && result.getInteger("code") != null && result.getInteger("code") == 200)
            {
                return result.getString("token");
            }
            else
            {
                throw new ServiceException(result != null ? result.getString("msg") : "外部系统登录失败");
            }
        }
        catch (Exception e)
        {
            throw new ServiceException("调用外部系统登录失败：" + e.getMessage());
        }
    }

    /**
     * 校验验证码
     * 
     * @param username 用户名
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public void validateCaptcha(String username, String code, String uuid)
    {
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled)
        {
            String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
            String captcha = redisCache.getCacheObject(verifyKey);
            if (captcha == null)
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire")));
                throw new CaptchaExpireException();
            }
            redisCache.deleteObject(verifyKey);
            if (!code.equalsIgnoreCase(captcha))
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
                throw new CaptchaException();
            }
        }
    }



    /**
     * 登录前置校验
     * @param username 用户名
     * @param password 用户密码
     */
    public void loginPreCheck(String username, String password)
    {
        // 用户名或密码为空 错误
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("not.null")));
            throw new UserNotExistsException();
        }
        // 密码如果不在指定范围内 错误
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
        // 用户名不在指定范围内 错误
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
        // IP黑名单校验
        String blackStr = configService.selectConfigByKey("sys.login.blackIPList");
        if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr()))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("login.blocked")));
            throw new BlackListException();
        }
    }



    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    public void recordLoginInfo(Long userId)
    {
        userService.updateLoginInfo(userId, IpUtils.getIpAddr(), DateUtils.getNowDate());
    }
}
