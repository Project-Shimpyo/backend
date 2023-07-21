package com.oneline.shimpyo.service.impl;

import com.oneline.shimpyo.domain.BaseException;
import com.oneline.shimpyo.domain.house.*;
import com.oneline.shimpyo.domain.house.dto.*;
import com.oneline.shimpyo.domain.member.Member;
import com.oneline.shimpyo.domain.review.dto.ReviewStatusCount;
import com.oneline.shimpyo.domain.room.Room;
import com.oneline.shimpyo.domain.room.RoomImage;
import com.oneline.shimpyo.domain.room.dto.RoomInfo;
import com.oneline.shimpyo.modules.S3FileHandler;
import com.oneline.shimpyo.repository.*;
import com.oneline.shimpyo.repository.dsl.HouseQuerydsl;
import com.oneline.shimpyo.repository.dsl.ReviewQuerydsl;
import com.oneline.shimpyo.service.HouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.oneline.shimpyo.domain.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HouseServiceImpl implements HouseService {
    private final HouseRepository houseRepository;
    private final RoomRepository roomRepository;
    private final HouseOptionRepository houseOptionRepository;
    private final HouseAddressRepository houseAddressRepository;
    private final HouseImageRepository houseImageRepository;
    private final RoomImageRepository roomImageRepository;
    private final S3FileHandler s3FileHandler;
    private final HouseQuerydsl houseQuerydsl;
    private final ReviewQuerydsl reviewQuerydsl;

    @Override
    @Transactional
    public long createHouse(Member member, PostHouseReq houseReq, List<MultipartFile> houseImages, List<MultipartFile> roomImages) throws BaseException {
        // 1-1. 숙소 이미지 S3 버킷 저장
        List<FileReq> savedHouseImages = new ArrayList<>();
        for (MultipartFile houseImage : houseImages) {
            Optional<FileReq> uploadedFile = s3FileHandler.uploadFile(houseImage);
            if (uploadedFile.isPresent()) {
                savedHouseImages.add(uploadedFile.get());
            } else {
                new BaseException(AWS_S3_EXCEPTION);
            }
        }
        // 1-2. 저장된 파일 정보 DTO에 저장
        houseReq.setFiles(savedHouseImages);

        // 2-1. 객실 이미지 S3 버킷 저장
        List<FileReq> savedRoomImages = new ArrayList<>();
        for (MultipartFile roomImage : roomImages) {
            Optional<FileReq> uploadedFile = s3FileHandler.uploadFile(roomImage);
            if (uploadedFile.isPresent()) {
                savedRoomImages.add(uploadedFile.get());
            } else {
                new BaseException(AWS_S3_EXCEPTION);
            }
        }
        // 2-2. 객실별 저장된 파일 정보 개별 객실 DTO에 저장
        int startNum = 0;
        for (int i = 0; i < houseReq.getRooms().size(); i++) {
            if (i > 0) {
                startNum += houseReq.getRooms().get(i-1).getImageCount();
            }
            List<FileReq> splitedImages = savedRoomImages.subList(startNum, startNum + houseReq.getRooms().get(i).getImageCount());
            houseReq.getRooms().get(i).setFiles(splitedImages);
        }

        // 1. 숙소정보 저장
        House toSaveHouse = House.builder()
                .member(member)
                .name(houseReq.getName())
                .type(houseReq.getType())
                .contents(houseReq.getContents())
                .build();
        House savedHouse = houseRepository.save(toSaveHouse);


        // 2. 숙소 옵션 저장
        List<HouseOptions> toSaveOptions = new ArrayList<>();
        if (houseReq.getOption().isWifi()) {
            toSaveOptions.add(HouseOptions.builder()
                    .name("wifi")
                    .house(savedHouse)
                    .build());
        }
        if (houseReq.getOption().isPc()) {
            toSaveOptions.add(HouseOptions.builder()
                    .name("pc")
                    .house(savedHouse)
                    .build());
        }
        if (houseReq.getOption().isParking()) {
            toSaveOptions.add(HouseOptions.builder()
                    .name("parking")
                    .house(savedHouse)
                    .build());
        }
        if (houseReq.getOption().isBbq()) {
            toSaveOptions.add(HouseOptions.builder()
                    .name("bbq")
                    .house(savedHouse)
                    .build());
        }
        houseOptionRepository.saveAll(toSaveOptions);

        // 3. 숙소 주소 저장
        HouseAddress toSaveAddress = HouseAddress.builder()
                .postCode(houseReq.getAddress().getPostCode())
                .sido(houseReq.getAddress().getSido())
                .sigungu(houseReq.getAddress().getSigungu())
                .fullAddress(houseReq.getAddress().getFullAddress())
                .lat(houseReq.getAddress().getLat())
                .lng(houseReq.getAddress().getLng())
                .house(savedHouse)
                .build();
        houseAddressRepository.save(toSaveAddress);

        // 4. 숙소 이미지 저장 (객실이미지X)
        List<HouseImage> toSaveHouseImages = new ArrayList<>();
        for (int i = 0; i < houseReq.getFiles().size(); i++) {
            HouseImage toSaveHouseImage = HouseImage.builder()
                    .house(savedHouse)
                    .originalFileName(houseReq.getFiles().get(i).getOriginalFileName())
                    .savedURL(houseReq.getFiles().get(i).getSavedURL())
                    .build();
            toSaveHouseImages.add(toSaveHouseImage);
        }
        houseImageRepository.saveAll(toSaveHouseImages);

        // 5-1. 객실 저장
        for (int i = 0; i < houseReq.getRooms().size(); i++) {
            Room toSaveRoom = Room.builder()
                    .name(houseReq.getRooms().get(i).getName())
                    .price(houseReq.getRooms().get(i).getPrice())
                    .house(savedHouse)
                    .minPeople(houseReq.getRooms().get(i).getMinPeople())
                    .maxPeople(houseReq.getRooms().get(i).getMaxPeople())
                    .bedCount(houseReq.getRooms().get(i).getBedCount())
                    .bedroomCount(houseReq.getRooms().get(i).getBedroomCount())
                    .bathroomCount(houseReq.getRooms().get(i).getBathroomCount())
                    .totalCount(houseReq.getRooms().get(i).getTotalCount())
                    .checkIn(houseReq.getRooms().get(i).getCheckIn())
                    .checkOut(houseReq.getRooms().get(i).getCheckOut())
                    .build();
            Room savedRoom = roomRepository.save(toSaveRoom);
            // 5-2. 객실 이미지 저장
            List<RoomImage> toSaveRoomImages = new ArrayList<>();
            for (int j = 0; j < houseReq.getRooms().get(i).getFiles().size(); j++) {
                RoomImage toSaveRoomImage = RoomImage.builder()
                        .room(savedRoom)
                        .originalFileName(houseReq.getRooms().get(i).getFiles().get(j).getOriginalFileName())
                        .savedURL(houseReq.getRooms().get(i).getFiles().get(j).getSavedURL())
                        .build();
                toSaveRoomImages.add(toSaveRoomImage);
            }
            roomImageRepository.saveAll(toSaveRoomImages);
        }
        return savedHouse.getId();
    }

    @Override
    public GetHouseDetailRes readHouseDetail(long houseId) {
        // 숙소 관련 정보
        HouseInfo foundHouse = Optional.ofNullable(houseQuerydsl.findHouseAndAddressByHouseId(houseId))
                .orElseThrow(() -> new BaseException(HOUSE_NONEXISTENT));
        foundHouse.setOptions(houseQuerydsl.findHouseOptionsByHouseId(houseId));
        foundHouse.setHouseImages(houseQuerydsl.findHouseImagesByHouseId(houseId));
        foundHouse.setReviewStatusCount(reviewQuerydsl.findCountByHouseId(houseId));

        // 객실 관련 정보
        List<RoomInfo> foundRooms = houseQuerydsl.findRoomsByHouseId(houseId);
        LocalDateTime checkIn = LocalDateTime.now();
        LocalDateTime checkOut = checkIn.plus(1, ChronoUnit.DAYS);
        // 객실 이미지 저장 및 품절여부 체크
        for (int i = 0; i < foundRooms.size(); i++) {
            // 이미지 저장
            List<String> foundRoomImages = houseQuerydsl.findRoomImagesByRoomId(foundRooms.get(i).getRoomId());
            foundRooms.get(i).setRoomImages(foundRoomImages);

            // 품절 여부 체크
            if(houseQuerydsl.checkReservation(foundRooms.get(i).getRoomId(), checkIn, checkOut)) foundRooms.get(i).setSoldout(true);
        }

        // Response DTO에 저장
        return GetHouseDetailRes.builder()
                .house(foundHouse)
                .rooms(foundRooms)
                .build();
    }

    @Override
    public List<GetHouseListRes> readHouseList(Pageable pageable, SearchFilterReq searchFilter, Member member) {
        List<GetHouseListRes> foundHouseList = houseQuerydsl.findAllHouse(pageable, searchFilter, member);
        return foundHouseList;
    }

    @Override
    @Transactional
    public void updateHouse(Member member, long houseId, PatchHouseReq patchHouseReq, List<MultipartFile> houseImages) {
        House foundHouse = houseRepository.findById(houseId)
                .orElseThrow(() -> new BaseException(HOUSE_NONEXISTENT));
        if (foundHouse.getMember().getId() != member.getId()) throw new BaseException(HOUSE_MEMBER_WRONG);

        // 숙소 기본 정보 수정
        foundHouse.setName(patchHouseReq.getName());
        foundHouse.setType(patchHouseReq.getType());
        foundHouse.setContents(patchHouseReq.getContents());

        // 숙소 옵션 수정 (삭제 후 재추가)
        houseOptionRepository.deleteAllByHouseId(foundHouse.getId());
        List<HouseOptions> toSaveOptions = new ArrayList<>();
        if (patchHouseReq.getOption().isWifi()) {
            toSaveOptions.add(HouseOptions.builder()
                    .name("wifi")
                    .house(foundHouse)
                    .build());
        }
        if (patchHouseReq.getOption().isPc()) {
            toSaveOptions.add(HouseOptions.builder()
                    .name("pc")
                    .house(foundHouse)
                    .build());
        }
        if (patchHouseReq.getOption().isParking()) {
            toSaveOptions.add(HouseOptions.builder()
                    .name("parking")
                    .house(foundHouse)
                    .build());
        }
        if (patchHouseReq.getOption().isBbq()) {
            toSaveOptions.add(HouseOptions.builder()
                    .name("bbq")
                    .house(foundHouse)
                    .build());
        }
        houseOptionRepository.saveAll(toSaveOptions);

        // 숙소 주소 수정
        HouseAddress foundAddress = houseAddressRepository.findByHouseId(foundHouse.getId())
                .orElseThrow(() -> new BaseException(ADDRESS_NONEXISTENT));
        foundAddress.setSido(patchHouseReq.getAddress().getSido());
        foundAddress.setPostCode(patchHouseReq.getAddress().getPostCode());
        foundAddress.setSigungu(patchHouseReq.getAddress().getSigungu());
        foundAddress.setFullAddress(patchHouseReq.getAddress().getFullAddress());
        foundAddress.setLat(patchHouseReq.getAddress().getLat());
        foundAddress.setLng(patchHouseReq.getAddress().getLng());

        // 파일 업로드 수정
        if (patchHouseReq.getPatchImageReqs() != null) {
            int houseImageIdx = 0;
            for (int i = 0; i < patchHouseReq.getPatchImageReqs().size(); i++) {
                int imageCount = patchHouseReq.getPatchImageReqs().get(i).getImageCount();
                ImageStatus imageStatus = patchHouseReq.getPatchImageReqs().get(i).getImageStatus();
                if (imageStatus.equals(ImageStatus.ADD)){
                    List<HouseImage> foundImages = houseImageRepository.findAllByHouseId(foundHouse.getId());
                    if (foundImages.size() == 5) throw new BaseException(IMAGE_STATUS_FULL);
                    Optional<FileReq> uploadedFile = s3FileHandler.uploadFile(houseImages.get(houseImageIdx));
                    if (uploadedFile.isPresent()) {
                        HouseImage toSaveHouseImage = HouseImage.builder()
                                .house(foundHouse)
                                .originalFileName(uploadedFile.get().getOriginalFileName())
                                .savedURL(uploadedFile.get().getSavedURL())
                                .build();
                        houseImageRepository.save(toSaveHouseImage);
                        houseImageIdx++;
                    }
                }
                else if (imageStatus.equals(ImageStatus.MODIFY)) {
                    Optional<FileReq> uploadedFile = s3FileHandler.uploadFile(houseImages.get(houseImageIdx));
                    if (uploadedFile.isPresent()) {
                        List<HouseImage> foundImages = houseImageRepository.findAllByHouseId(foundHouse.getId());
                        // S3 버킷에 존재하는 기존 파일 삭제
                        s3FileHandler.removeFile(foundImages.get(imageCount).getSavedURL());
                        // 새로 저장한 파일 정보로 업데이트
                        foundImages.get(imageCount).setOriginalFileName(uploadedFile.get().getOriginalFileName());
                        foundImages.get(imageCount).setSavedURL(uploadedFile.get().getSavedURL());
                        houseImageIdx++;
                    }
                } else {
                    List<HouseImage> foundImages = houseImageRepository.findAllByHouseId(foundHouse.getId());
                    // S3 버킷에 존재하는 기존 파일 삭제
                    s3FileHandler.removeFile(foundImages.get(imageCount).getSavedURL());
                    // DB에 파일 정보 삭제
                    houseImageRepository.deleteById(foundImages.get(imageCount).getId());
                }
            }
        }
    }

    @Override
    @Transactional
    public void deleteHouse(Member member, long houseId) {
        House foundHouse = houseRepository.findById(houseId)
                .orElseThrow(() -> new BaseException(HOUSE_NONEXISTENT));
        if (member.getId() != foundHouse.getMember().getId()) throw new BaseException(HOUSE_MEMBER_WRONG);
        // AWS의 숙소 이미지 삭제
        List<HouseImage> foundHouseImages = houseImageRepository.findAllByHouseId(foundHouse.getId());
        for (HouseImage houseImage : foundHouseImages) {
            s3FileHandler.removeFile(houseImage.getSavedURL());
        }
        // AWS의 객실 이미지 삭제
        List<Room> foundRooms = roomRepository.findAllByHouseId(foundHouse.getId());
        for (Room room : foundRooms) {
            for (RoomImage roomImage : room.getImages()) {
                s3FileHandler.removeFile(roomImage.getSavedURL());
            }
        }
        // 숙소 관련 모든 정보 삭제(CASCADE)
        houseRepository.deleteById(houseId);
    }

    @Override
    public List<GetMyHouseListRes> readMyHouseList(long memberId) {
        return houseQuerydsl.readMyHouseList(memberId);
    }

}
