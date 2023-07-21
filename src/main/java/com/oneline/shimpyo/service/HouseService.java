package com.oneline.shimpyo.service;

import com.oneline.shimpyo.domain.house.dto.*;
import com.oneline.shimpyo.domain.member.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface HouseService {

    long createHouse(Member member, PostHouseReq houseReq, List<MultipartFile> houseImages, List<MultipartFile> roomImages);
    GetHouseDetailRes readHouseDetail(long houseId);
    List<GetHouseListRes> readHouseList(Pageable pageable, SearchFilterReq searchFilter, Member member);
    void updateHouse(Member member, long houseId, PatchHouseReq patchHouseReq, List<MultipartFile> houseImages);
    void deleteHouse(Member member, long houseId);

    List<GetMyHouseListRes> readMyHouseList(long memberId);
}
