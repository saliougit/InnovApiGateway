// package com.innov4africa.api_gateway.repository;


// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.stereotype.Repository;

// @Repository
// public class TokenRepository {

//     @Autowired
//     private RedisTemplate<String, String> redisTemplate;

//     public void saveToken(String serviceName, String token) {
//         redisTemplate.opsForValue().set(serviceName, token);
//     }

//     public String getToken(String serviceName) {
//         return redisTemplate.opsForValue().get(serviceName);
//     }

//     public void deleteToken(String serviceName) {
//         redisTemplate.delete(serviceName);
//     }
// }


package com.innov4africa.api_gateway.repository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TokenRepository {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void saveToken(String serviceName, String token) {
        redisTemplate.opsForValue().set(serviceName, token);
    }

    public String getToken(String serviceName) {
        return redisTemplate.opsForValue().get(serviceName);
    }

    public void deleteToken(String serviceName) {
        redisTemplate.delete(serviceName);
    }
}