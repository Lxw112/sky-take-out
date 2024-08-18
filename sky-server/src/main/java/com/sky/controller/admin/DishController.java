package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 新增菜品
     * @param dto
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dto){
        log.info("新增菜品：{}",dto);
        dishService.saveWithFlavor(dto);
        //清理缓存数据
        String key = "dish_" + dto.getCategoryId();
        clearCache(key);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页·查询：{}",dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 菜品批量删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("菜品批量删除")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result delete(@RequestParam List<Long> ids){
        log.info("菜品批量删除:{}",ids);
        dishService.deleteBatch(ids);
        //将所有的菜品缓存数据清理掉，所有以dish_开头的
        clearCache("dish_*");
        return Result.success();
    }
    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售停售")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result startOrStop(@PathVariable Integer status,Long id){
        dishService.startOrStop(status,id);
        //将所有的菜品缓存数据清理掉，所有以dish_开头的
        clearCache("dish_*");
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> GetById(@PathVariable Long id){
        log.info("根据id查询菜品：{}",id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品
     * @param dto
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public Result update(@RequestBody DishDTO dto){
        log.info("修改菜品：{}",dto);
        dishService.updateWithFlavor(dto);
        //将所有的菜品缓存数据清理掉，所有以dish_开头的
        clearCache("dish_*");
        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> getByCategoryId(Long categoryId,String name){
        log.info("根据分类id查询菜品，{}",categoryId);
        List<Dish> dishes = dishService.getByCategoryId(categoryId,name);
        return Result.success(dishes);
    }

    /**
     * 清理缓存数据
     * @param pattern
     */
    private void clearCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
