package com.wedit.backend.api.scrap.service;

import com.wedit.backend.api.member.entity.Member;
import com.wedit.backend.api.member.repository.MemberRepository;
import com.wedit.backend.api.scrap.dto.ScrapListResponseDTO;
import com.wedit.backend.api.scrap.dto.ScrapToggleResponseDTO;
import com.wedit.backend.api.scrap.entity.Scrap;
import com.wedit.backend.api.scrap.repository.ScrapRepository;
import com.wedit.backend.api.vendor.entity.Vendor;
import com.wedit.backend.api.vendor.entity.VendorCategory;
import com.wedit.backend.api.vendor.entity.VendorMedia;
import com.wedit.backend.api.vendor.repository.VendorRepository;
import com.wedit.backend.common.exception.NotFoundException;
import com.wedit.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final MemberRepository memberRepository;
    private final VendorRepository vendorRepository;

    @Transactional
    public ScrapToggleResponseDTO toggle(Long memberId, Long vendorId) {

        vendorRepository.findActiveDetailById(vendorId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_VENDOR.getMessage()));

        return scrapRepository.findByMemberIdAndVendorId(memberId, vendorId)
                .map(scrap -> {
                    scrapRepository.delete(scrap);
                    return new ScrapToggleResponseDTO(false);
                })
                .orElseGet(() -> {
                    Member member = memberRepository.getReferenceById(memberId);
                    Vendor vendor = vendorRepository.getReferenceById(vendorId);
                    scrapRepository.save(Scrap.of(member, vendor));
                    return ScrapToggleResponseDTO.of(true);
                });
    }

    @Transactional(readOnly = true)
    public ScrapToggleResponseDTO getStatus(Long memberId, Long vendorId) {

        boolean isScraped = scrapRepository.existsByMemberIdAndVendorId(memberId, vendorId);

        return ScrapToggleResponseDTO.of(isScraped);
    }

    @Transactional(readOnly = true)
    public Page<ScrapListResponseDTO> getScrapList(Long memberId, VendorCategory category, Pageable pageable) {

        Page<Scrap> scraps = (category == null)
                ? scrapRepository.findScrapsByMemberId(memberId, pageable)
                : scrapRepository.findScrapsByMemberIdAndCategory(memberId, category.name(), pageable);

        return scraps.map(scrap -> {
            Vendor vendor = scrap.getVendor();

            String thumbnailUrl = vendor.getMediaList().stream()
                    .filter(VendorMedia::isThumbnail)
                    .findFirst()
                    .or(() -> vendor.getMediaList().stream().findFirst())
                    .map(media -> media.getUrl())
                    .orElse(null);

            long basePrice = vendor.getItemGroups().stream()
                    .filter(ig -> !ig.isDeleted())
                    .mapToLong(ig -> ig.getCachedMinPrice() != null ? ig.getCachedMinPrice() : 0L)
                    .min()
                    .orElse(0L);

            return ScrapListResponseDTO.of(
                    vendor.getId(),
                    vendor.getName(),
                    vendor.getVendorCategory(),
                    vendor.getRegion(),
                    thumbnailUrl,
                    basePrice
            );
        });
    }
}
