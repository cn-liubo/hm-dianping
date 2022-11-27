package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryList() {
        //1.redis中查询商铺类型缓存
        String key = RedisConstants.CACHE_SHOPTYPE_KEY;
        List<String> typeList = stringRedisTemplate.opsForList().range(key, 0, -1);
        //2.判断是否存在
        List<ShopType> shopTypeList = new ArrayList<>();
        if (!typeList.isEmpty()) {
            //3.存在，直接返回
            for (String s : typeList) {
                shopTypeList.add(JSONUtil.toBean(s, ShopType.class));
            }
            return Result.ok(shopTypeList);
        }
        //4.不存在，则直接查询数据库
        shopTypeList = query().orderByAsc("sort").list();
        //5.不存在，返回错误
        if (shopTypeList == null) {
            return Result.fail("店铺类型不存在！");
        }
        //6.存在，先写入redis
        for (ShopType shopType : shopTypeList) {
            stringRedisTemplate.opsForList().rightPush(key, JSONUtil.toJsonStr(shopType));
        }
        //7.返回
        return Result.ok(shopTypeList);
    }
}
