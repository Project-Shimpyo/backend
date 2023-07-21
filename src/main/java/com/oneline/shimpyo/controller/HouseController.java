package com.oneline.shimpyo.controller;

import com.oneline.shimpyo.domain.BaseResponse;
import com.oneline.shimpyo.domain.house.dto.*;
import com.oneline.shimpyo.domain.member.Member;
import com.oneline.shimpyo.modules.CheckMember;
import com.oneline.shimpyo.security.auth.CurrentMember;
import com.oneline.shimpyo.service.HouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class HouseController {

    private final HouseService houseService;
    private final CheckMember checkMember;

    @PostMapping(value = "/user/houses", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<HouseRegisterRes> createHouse(@RequestPart(value = "houseReq") PostHouseReq houseReq, @RequestPart(value = "houseImages") List<MultipartFile> houseImages
                                , @RequestPart(value = "roomImages") List<MultipartFile> roomImages, @CurrentMember Member member) {

        long houseId = houseService.createHouse(member, houseReq, houseImages, roomImages);

        return new BaseResponse<>(new HouseRegisterRes(houseId));
    }

    @GetMapping("/api/houses/{houseId}")
    public BaseResponse<GetHouseDetailRes> readHouseDetail(@PathVariable long houseId) {
        GetHouseDetailRes getHouseDetailRes = houseService.readHouseDetail(houseId);
        return new BaseResponse<>(getHouseDetailRes);
    }

    @PostMapping("/api/houses")
    public BaseResponse<List<GetHouseListRes>> readHouseList(@RequestBody SearchFilterReq searchFilter, @CurrentMember Member member) {
        // city : 시/도,  district : 시/군/구,  checkin : 체크인,  checkout : 체크아웃,  people : 인원수
        PageRequest pageRequest = PageRequest.of(searchFilter.getPage(), 20);
        List<GetHouseListRes> foundHouseList = houseService.readHouseList(pageRequest, searchFilter, member);
        return new BaseResponse<>(foundHouseList);
    }


    @PatchMapping("/user/houses/{houseId}")
    public BaseResponse<Void> updateHouse(@CurrentMember Member member, @PathVariable long houseId, @RequestPart PatchHouseReq patchHouseReq
                            , @RequestPart(value = "houseImages", required = false) List<MultipartFile> houseImages) {
        houseService.updateHouse(member, houseId, patchHouseReq, houseImages);
        return new BaseResponse<>();
    }

    @DeleteMapping("/user/houses/{houseId}")
    public BaseResponse<Void> deleteHouse(@CurrentMember Member member, @PathVariable long houseId) {
        houseService.deleteHouse(member, houseId);
        return new BaseResponse<>();
    }

    @GetMapping("/user/houses")
    public BaseResponse<List<GetMyHouseListRes>> readMyHouseList(@CurrentMember Member member){
        long memberId = checkMember.getMemberId(member, true);
        return new BaseResponse<>(houseService.readMyHouseList(memberId));
    }

}
