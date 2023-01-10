package com.example.blogapi.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.blogapi.dao.mapper.SysUserMapper;
import com.example.blogapi.dao.pojo.SysUser;
import com.example.blogapi.service.LoginService;
import com.example.blogapi.service.SysUserService;
import com.example.blogapi.utils.JWTUtils;
import com.example.blogapi.vo.ErrorCode;
import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.UserVo;
import com.example.blogapi.vo.params.LoginParam;
import io.netty.util.internal.StringUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private  RedisTemplate<String, String> redisTemplate;

    private static final String slat = "mszlu!@#";
    @Override
    public Result login(LoginParam loginParam) {
        /*
        * 1.检测参数是否合法
        * 2.根据用户名和密码去user表中查询
        * 3. 如果不存在 登录失败
        * 4 如果存在，使用jwt生成token，返回前端
        * 5.token放入redis中 登录认证时，先认证token是否合法，再去redis认证是否存在。
        * */

        String account=loginParam.getAccount();
        String password = loginParam.getPassword();
        if(StringUtils.isBlank(account)||StringUtils.isBlank(password))
        {
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), ErrorCode.PARAMS_ERROR.getMsg());
        }
        password= DigestUtils.md5Hex(password+slat);
        SysUser sysUser=sysUserService.findUser(account,password);
        if(sysUser==null)
        {
            return  Result.fail(ErrorCode.ACCOUNT_PWD_NOT_EXIST.getCode(), ErrorCode.ACCOUNT_PWD_NOT_EXIST.getMsg());
        }
        String token = JWTUtils.createToken(sysUser.getId());
        redisTemplate.opsForValue().set("TOKEN_"+token, JSON.toJSONString(sysUser),1, TimeUnit.DAYS);
        return  Result.success(token);
    }
    /*
    * 1.token合法性检验 是否为空，解析是否成功，redis是否存在
    * 2.如果失败，返回错误
    * 3.成功返回对应结果
    * */
    @Override
    public SysUser checkToken(String token) {

        if(StringUtils.isBlank(token))
        {
            return  null;
        }
        Map<String, Object> stringObjectMap = JWTUtils.checkToken(token);
        if(stringObjectMap==null)
        {
            return  null;
        }
        String userJson = redisTemplate.opsForValue().get("TOKEN_" + token);
        if(userJson.length()==0)
        {
            return  null;
        }
        return JSON.parseObject(userJson, SysUser.class);

    }

    @Override
    public Result logout(String token) {
        redisTemplate.delete("TOKEN_"+token);
        return Result.success(null);
    }

    @Override
    @Transactional
    public Result register(LoginParam loginParam) {
        /*
        * 1.判断参数是否合法
        * 2.判断用户是否存在，存在，返回账户已注册
        * 3.不存在，注册账户
        * 4.生成token
        * 5.存入redis
        * 6.事务，出现问题，注册的账户 回滚
        * */
        String account = loginParam.getAccount();
        String password = loginParam.getPassword();
        String nickname = loginParam.getNickname();
        if(StringUtils.isBlank(account)||StringUtils.isBlank(password)||StringUtils.isBlank(nickname))
        {
            return  Result.fail((ErrorCode.PARAMS_ERROR.getCode()),ErrorCode.PARAMS_ERROR.getMsg());
        }
        SysUser sysUser=sysUserService.findUserByAccount(account);
        if(sysUser!=null)
        {
            return  Result.fail(ErrorCode.ACCOUNT_EXIST.getCode(),ErrorCode.ACCOUNT_EXIST.getMsg());
        }
        sysUser = new SysUser();
        sysUser.setNickname(nickname);
        sysUser.setAccount(account);
        sysUser.setPassword(DigestUtils.md5Hex(password+slat));
        sysUser.setCreateDate(System.currentTimeMillis());
        sysUser.setLastLogin(System.currentTimeMillis());
        sysUser.setAvatar("/static/img/logo.b3a48c0.png");
        sysUser.setAdmin(1); //1 为true
        sysUser.setDeleted(0); // 0 为false
        sysUser.setSalt("");
        sysUser.setStatus("");
        sysUser.setEmail("");
        this.sysUserService.save(sysUser);
        String token = JWTUtils.createToken(sysUser.getId());
        redisTemplate.opsForValue().set("TOKEN_"+token, JSON.toJSONString(sysUser),1, TimeUnit.DAYS);
        return  Result.success(token);
    }
}
