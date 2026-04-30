package com.wedit.backend.api.scrap.service;

import com.wedit.backend.api.member.entity.Member;
import com.wedit.backend.api.member.repository.MemberRepository;
import com.wedit.backend.api.scrap.dto.ScrapToggleResponseDTO;
import com.wedit.backend.api.scrap.entity.Scrap;
import com.wedit.backend.api.scrap.repository.ScrapRepository;
import com.wedit.backend.api.vendor.entity.Vendor;
import com.wedit.backend.api.vendor.repository.VendorRepository;
import com.wedit.backend.common.exception.NotFoundException;
import com.wedit.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
