package com.example.diet.food.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.diet.food.domain.model.FoodCategory;
import com.example.diet.food.domain.model.FoodCategoryId;
import com.example.diet.food.domain.repository.FoodCategoryRepository;
import com.example.diet.food.infrastructure.persistence.mapper.FoodCategoryMapper;
import com.example.diet.food.infrastructure.persistence.po.FoodCategoryPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 食物分类仓储实现
 */
@Repository
@RequiredArgsConstructor
public class FoodCategoryRepositoryImpl implements FoodCategoryRepository {

    private final FoodCategoryMapper categoryMapper;

    @Override
    public FoodCategory save(FoodCategory category) {
        FoodCategoryPO po = toPO(category);
        if (po.getId() == null) {
            categoryMapper.insert(po);
        } else {
            categoryMapper.updateById(po);
        }
        category.assignId(FoodCategoryId.of(po.getId()));
        return category;
    }

    @Override
    public Optional<FoodCategory> findById(FoodCategoryId id) {
        FoodCategoryPO po = categoryMapper.selectById(id.getValue());
        return Optional.ofNullable(po).map(this::toDomain);
    }

    @Override
    public List<FoodCategory> findAll() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<FoodCategoryPO>()
                        .orderByAsc(FoodCategoryPO::getSortOrder)
        ).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(FoodCategoryId id) {
        categoryMapper.deleteById(id.getValue());
    }

    @Override
    public boolean existsById(FoodCategoryId id) {
        return categoryMapper.selectById(id.getValue()) != null;
    }

    @Override
    public long countFoodsByCategoryId(FoodCategoryId id) {
        return categoryMapper.countFoodsByCategoryId(id.getValue());
    }

    private FoodCategoryPO toPO(FoodCategory category) {
        FoodCategoryPO po = new FoodCategoryPO();
        if (category.getId() != null) {
            po.setId(category.getId().getValue());
        }
        po.setName(category.getName());
        po.setDescription(category.getDescription());
        po.setColor(category.getColor());
        po.setSortOrder(category.getSortOrder());
        po.setCreatedAt(category.getCreatedAt());
        po.setUpdatedAt(category.getUpdatedAt());
        return po;
    }

    private FoodCategory toDomain(FoodCategoryPO po) {
        return FoodCategory.reconstitute(
                FoodCategoryId.of(po.getId()),
                po.getName(),
                po.getDescription(),
                po.getColor(),
                po.getSortOrder(),
                po.getCreatedAt(),
                po.getUpdatedAt()
        );
    }
}
