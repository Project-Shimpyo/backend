package com.oneline.shimpyo.repository.dsl;

import com.oneline.shimpyo.domain.wish.dto.GetWishRes;
import com.oneline.shimpyo.domain.wish.dto.QGetWishRes;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import java.util.List;

import static com.oneline.shimpyo.domain.house.QHouse.house;
import static com.oneline.shimpyo.domain.house.QHouseImage.houseImage;
import static com.oneline.shimpyo.domain.wish.QWish.wish;

@Repository
public class WishQuerydsl {
    private final JPAQueryFactory jqf;

    public WishQuerydsl(EntityManager em) {
        this.jqf = new JPAQueryFactory(em);
    }

    public long countByMemberIdAndHouseId(long memberId){
        return jqf.select(wish.count())
                .from(wish)
                .where(wish.member.id.eq(memberId))
                .fetchOne();
    }

    public List<GetWishRes> findAllByMemberId(long memberId) {
        return jqf.select(new QGetWishRes(wish.house.id, house.name, house.type, houseImage.savedURL))
                .from(wish)
                .join(houseImage)
                .on(wish.house.id.eq(houseImage.house.id))
                .join(house)
                .on(wish.house.id.eq(house.id))
                .where(houseImage.id.eq(JPAExpressions.select(houseImage.id.min())
                                                        .from(houseImage)
                                                        .where(house.id.eq(houseImage.house.id))), wish.member.id.eq(memberId))
                .fetch();
    }
}
