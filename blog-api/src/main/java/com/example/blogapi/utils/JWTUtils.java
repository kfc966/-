package com.example.blogapi.utils;

import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JWTUtils {
    private static final  String jwtToken="123456cdf@@!kk";
    public  static  String createToken(Long userId)
    {
        Map<String,Object> claims=new HashMap<>();              //要加密的数据
        claims.put("userId",userId);
        JwtBuilder jwtBuilder = Jwts.builder()
                .signWith(SignatureAlgorithm.HS256,jwtToken)   //签发算法，密钥为jwtToken
                .setClaims(claims)         //body 数据
                .setIssuedAt(new Date())   //签发时间
                .setExpiration(new Date((System.currentTimeMillis())+24*60*60*1000)); //一天有效时间
        String token = jwtBuilder.compact();
        return  token;
    }
    public  static  Map<String, Object> checkToken(String token)
    {
        try {
            Jwt parse = Jwts.parser().setSigningKey(jwtToken).parse(token);
            return (Map<String, Object>) parse.getBody();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return  null;
    }

    public static void main(String[] args) {
        String token = JWTUtils.createToken(100L);
        System.out.println(token);
        System.out.println(checkToken(token));
    }
}
