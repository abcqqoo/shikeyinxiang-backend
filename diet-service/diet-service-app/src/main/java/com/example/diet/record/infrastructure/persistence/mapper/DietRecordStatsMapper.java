package com.example.diet.record.infrastructure.persistence.mapper;

import com.example.diet.record.infrastructure.persistence.dto.DailyNutritionTotalsDTO;
import com.example.diet.record.infrastructure.persistence.dto.DailyUserCountDTO;
import com.example.diet.record.infrastructure.persistence.dto.PopularFoodStatDTO;
import com.example.diet.record.infrastructure.persistence.dto.UserDailyNutritionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 饮食记录统计 Mapper
 */
@Mapper
public interface DietRecordStatsMapper {

    @Select("""
            SELECT dr.date AS record_date,
                   SUM(drf.calories) AS total_calories,
                   SUM(drf.protein) AS total_protein,
                   SUM(drf.fat) AS total_fat,
                   SUM(drf.carbs) AS total_carbs
            FROM diet_records dr
            JOIN diet_record_foods drf ON drf.diet_record_id = dr.id
            WHERE dr.date BETWEEN #{startDate} AND #{endDate}
            GROUP BY dr.date
            """)
    List<DailyNutritionTotalsDTO> selectDailyFoodTotals(@Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    @Select("""
            SELECT date AS record_date,
                   SUM(calories) AS total_calories,
                   SUM(protein) AS total_protein,
                   SUM(fat) AS total_fat,
                   SUM(carbs) AS total_carbs
            FROM recommended_recipe_diet_records
            WHERE date BETWEEN #{startDate} AND #{endDate}
            GROUP BY date
            """)
    List<DailyNutritionTotalsDTO> selectDailyRecipeTotals(@Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    @Select("""
            SELECT record_date,
                   COUNT(DISTINCT user_id) AS user_count
            FROM (
                SELECT dr.date AS record_date, dr.user_id
                FROM diet_records dr
                WHERE dr.date BETWEEN #{startDate} AND #{endDate}
                UNION ALL
                SELECT rr.date AS record_date, rr.user_id
                FROM recommended_recipe_diet_records rr
                WHERE rr.date BETWEEN #{startDate} AND #{endDate}
            ) t
            GROUP BY record_date
            """)
    List<DailyUserCountDTO> selectDailyActiveUsers(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    @Select("""
            SELECT dr.user_id AS user_id,
                   dr.date AS record_date,
                   SUM(drf.calories) AS total_calories,
                   SUM(drf.protein) AS total_protein,
                   SUM(drf.fat) AS total_fat,
                   SUM(drf.carbs) AS total_carbs
            FROM diet_records dr
            JOIN diet_record_foods drf ON drf.diet_record_id = dr.id
            WHERE dr.date = #{date}
            GROUP BY dr.user_id, dr.date
            """)
    List<UserDailyNutritionDTO> selectUserFoodTotals(@Param("date") LocalDate date);

    @Select("""
            SELECT user_id AS user_id,
                   date AS record_date,
                   SUM(calories) AS total_calories,
                   SUM(protein) AS total_protein,
                   SUM(fat) AS total_fat,
                   SUM(carbs) AS total_carbs
            FROM recommended_recipe_diet_records
            WHERE date = #{date}
            GROUP BY user_id, date
            """)
    List<UserDailyNutritionDTO> selectUserRecipeTotals(@Param("date") LocalDate date);

    @Select("""
            SELECT drf.food_id AS food_id,
                   drf.food_name AS name,
                   COUNT(*) AS count
            FROM diet_record_foods drf
            JOIN diet_records dr ON dr.id = drf.diet_record_id
            WHERE dr.date BETWEEN #{startDate} AND #{endDate}
              AND drf.food_id IS NOT NULL
            GROUP BY drf.food_id, drf.food_name
            ORDER BY count DESC
            LIMIT #{limit}
            """)
    List<PopularFoodStatDTO> selectPopularFoods(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate,
                                                @Param("limit") Integer limit);

    @Select("""
            SELECT NULL AS food_id,
                   recipe_name AS name,
                   COUNT(*) AS count
            FROM recommended_recipe_diet_records
            WHERE date BETWEEN #{startDate} AND #{endDate}
            GROUP BY recipe_name
            ORDER BY count DESC
            LIMIT #{limit}
            """)
    List<PopularFoodStatDTO> selectPopularRecipes(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate,
                                                  @Param("limit") Integer limit);
}
