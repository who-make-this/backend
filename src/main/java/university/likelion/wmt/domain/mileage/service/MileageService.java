package university.likelion.wmt.domain.mileage.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import university.likelion.wmt.domain.mileage.dto.response.BalanceResponse;
import university.likelion.wmt.domain.mileage.implement.MileageReader;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MileageService {
    private final MileageReader mileageReader;

    public BalanceResponse getBalance(long userId) {
        return new BalanceResponse(mileageReader.getBalance(userId));
    }
}
