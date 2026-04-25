package com.wedit.backend.api.vendor.service;

import com.wedit.backend.api.vendor.dto.VendorResponseDTO;
import com.wedit.backend.api.vendor.dto.VendorUpsertRequestDTO;
import com.wedit.backend.api.vendor.entity.Dress;
import com.wedit.backend.api.vendor.entity.Makeup;
import com.wedit.backend.api.vendor.entity.Studio;
import com.wedit.backend.api.vendor.entity.Vendor;
import com.wedit.backend.api.vendor.entity.VendorCategory;
import com.wedit.backend.api.vendor.entity.WeddingHall;
import com.wedit.backend.api.vendor.repository.VendorRepository;
import com.wedit.backend.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorService {

    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "id");

    private final VendorRepository vendorRepository;

    @Transactional
    public VendorResponseDTO createVendor(VendorUpsertRequestDTO request) {
        validateRequest(request);
        Vendor savedVendor = vendorRepository.save(buildVendor(request));
        return VendorResponseDTO.from(savedVendor);
    }

    @Transactional(readOnly = true)
    public List<VendorResponseDTO> getVendors(VendorCategory category, boolean includeInactive) {
        List<Vendor> vendors = includeInactive
                ? vendorRepository.findAll(DEFAULT_SORT)
                : vendorRepository.findAllByIsActiveTrue(DEFAULT_SORT);

        return vendors.stream()
                .filter(vendor -> category == null || vendor.getVendorCategory() == category)
                .map(VendorResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public VendorResponseDTO getVendor(Long vendorId, boolean includeInactive) {
        Vendor vendor = includeInactive
                ? findVendor(vendorId)
                : vendorRepository.findByIdAndIsActiveTrue(vendorId)
                        .orElseThrow(() -> new NotFoundException("업체를 찾을 수 없습니다."));

        return VendorResponseDTO.from(vendor);
    }

    @Transactional
    public VendorResponseDTO updateVendor(Long vendorId, VendorUpsertRequestDTO request) {
        Vendor vendor = findVendor(vendorId);

        if (vendor.getVendorCategory() != request.getCategory()) {
            throw new IllegalArgumentException("업체 카테고리는 변경할 수 없습니다.");
        }

        validateRequest(request);
        vendor.updateCommonInfo(
                request.getName(),
                request.getRegion(),
                request.getFullAddress(),
                request.getAddressDetail(),
                request.getContactInfo(),
                request.getLatitude(),
                request.getLongitude(),
                request.getKakaoMapUrl(),
                request.getWebsite(),
                request.getInstagramUrl(),
                request.getDescription()
        );
        applyCategorySpecificFields(vendor, request);

        return VendorResponseDTO.from(vendor);
    }

    @Transactional
    public void deleteVendor(Long vendorId) {
        Vendor vendor = findVendor(vendorId);
        if (vendor.isActive()) {
            vendor.deactivate();
        }
    }

    private Vendor findVendor(Long vendorId) {
        return vendorRepository.findById(vendorId)
                .orElseThrow(() -> new NotFoundException("업체를 찾을 수 없습니다."));
    }

    private Vendor buildVendor(VendorUpsertRequestDTO request) {
        VendorCategory category = request.getCategory();

        if (category == VendorCategory.WEDDING_HALL) {
            return WeddingHall.builder()
                    .name(request.getName())
                    .region(request.getRegion())
                    .fullAddress(request.getFullAddress())
                    .addressDetail(request.getAddressDetail())
                    .contactInfo(request.getContactInfo())
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .kakaoMapUrl(request.getKakaoMapUrl())
                    .website(request.getWebsite())
                    .instagramUrl(request.getInstagramUrl())
                    .description(request.getDescription())
                    .capacity(request.getCapacity())
                    .hallCount(request.getHallCount())
                    .mealAvailable(Boolean.TRUE.equals(request.getMealAvailable()))
                    .parkingAvailable(Boolean.TRUE.equals(request.getParkingAvailable()))
                    .build();
        }

        if (category == VendorCategory.STUDIO) {
            return Studio.builder()
                    .name(request.getName())
                    .region(request.getRegion())
                    .fullAddress(request.getFullAddress())
                    .addressDetail(request.getAddressDetail())
                    .contactInfo(request.getContactInfo())
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .kakaoMapUrl(request.getKakaoMapUrl())
                    .website(request.getWebsite())
                    .instagramUrl(request.getInstagramUrl())
                    .description(request.getDescription())
                    .isOutdoor(Boolean.TRUE.equals(request.getOutdoor()))
                    .photographerCount(request.getPhotographerCount())
                    .shootingStyle(request.getShootingStyle())
                    .build();
        }

        if (category == VendorCategory.DRESS) {
            return Dress.builder()
                    .name(request.getName())
                    .region(request.getRegion())
                    .fullAddress(request.getFullAddress())
                    .addressDetail(request.getAddressDetail())
                    .contactInfo(request.getContactInfo())
                    .latitude(request.getLatitude())
                    .longitude(request.getLongitude())
                    .kakaoMapUrl(request.getKakaoMapUrl())
                    .website(request.getWebsite())
                    .instagramUrl(request.getInstagramUrl())
                    .description(request.getDescription())
                    .brand(request.getBrand())
                    .fittingCount(request.getFittingCount())
                    .build();
        }

        return Makeup.builder()
                .name(request.getName())
                .region(request.getRegion())
                .fullAddress(request.getFullAddress())
                .addressDetail(request.getAddressDetail())
                .contactInfo(request.getContactInfo())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .kakaoMapUrl(request.getKakaoMapUrl())
                .website(request.getWebsite())
                .instagramUrl(request.getInstagramUrl())
                .description(request.getDescription())
                .artistCount(request.getArtistCount())
                .isHomeCareAvailable(Boolean.TRUE.equals(request.getHomeCareAvailable()))
                .homeCareFee(normalizeHomeCareFee(request))
                .build();
    }

    private void applyCategorySpecificFields(Vendor vendor, VendorUpsertRequestDTO request) {
        VendorCategory category = vendor.getVendorCategory();

        if (category == VendorCategory.WEDDING_HALL) {
            ((WeddingHall) vendor).updateDetails(
                    request.getCapacity(),
                    request.getHallCount(),
                    Boolean.TRUE.equals(request.getMealAvailable()),
                    Boolean.TRUE.equals(request.getParkingAvailable())
            );
            return;
        }

        if (category == VendorCategory.STUDIO) {
            ((Studio) vendor).updateDetails(
                    Boolean.TRUE.equals(request.getOutdoor()),
                    request.getPhotographerCount(),
                    request.getShootingStyle()
            );
            return;
        }

        if (category == VendorCategory.DRESS) {
            ((Dress) vendor).updateDetails(
                    request.getBrand(),
                    request.getFittingCount()
            );
            return;
        }

        ((Makeup) vendor).updateDetails(
                request.getArtistCount(),
                Boolean.TRUE.equals(request.getHomeCareAvailable()),
                normalizeHomeCareFee(request)
        );
    }

    private void validateRequest(VendorUpsertRequestDTO request) {
        if (request.getLatitude() == null ^ request.getLongitude() == null) {
            throw new IllegalArgumentException("위도와 경도는 함께 입력해야 합니다.");
        }

        VendorCategory category = request.getCategory();

        if (category == VendorCategory.WEDDING_HALL) {
            requireNotNull(request.getCapacity(), "웨딩홀 수용 인원은 필수입니다.");
            requireNotNull(request.getHallCount(), "웨딩홀 홀 수는 필수입니다.");
            requireNotNull(request.getMealAvailable(), "웨딩홀 식사 제공 여부는 필수입니다.");
            requireNotNull(request.getParkingAvailable(), "웨딩홀 주차 가능 여부는 필수입니다.");
            return;
        }

        if (category == VendorCategory.STUDIO) {
            requireNotNull(request.getOutdoor(), "스튜디오 야외 촬영 가능 여부는 필수입니다.");
            requireNotNull(request.getPhotographerCount(), "스튜디오 작가 수는 필수입니다.");
            requireText(request.getShootingStyle(), "스튜디오 촬영 스타일은 필수입니다.");
            return;
        }

        if (category == VendorCategory.DRESS) {
            requireText(request.getBrand(), "드레스 브랜드는 필수입니다.");
            requireNotNull(request.getFittingCount(), "드레스 피팅 횟수는 필수입니다.");
            return;
        }

        requireNotNull(request.getArtistCount(), "메이크업 아티스트 수는 필수입니다.");
        requireNotNull(request.getHomeCareAvailable(), "메이크업 출장 가능 여부는 필수입니다.");
        if (Boolean.TRUE.equals(request.getHomeCareAvailable()) && request.getHomeCareFee() == null) {
            throw new IllegalArgumentException("출장 가능 메이크업은 출장 비용이 필수입니다.");
        }
    }

    private void requireNotNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private void requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
    }

    private Long normalizeHomeCareFee(VendorUpsertRequestDTO request) {
        return Boolean.TRUE.equals(request.getHomeCareAvailable()) ? request.getHomeCareFee() : null;
    }
}
