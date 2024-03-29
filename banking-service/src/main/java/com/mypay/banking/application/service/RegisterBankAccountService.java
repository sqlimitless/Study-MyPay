package com.mypay.banking.application.service;

import com.mypay.banking.adapter.out.external.bank.BankAccount;
import com.mypay.banking.adapter.out.external.bank.GetBankAccountRequest;
import com.mypay.banking.adapter.out.persistence.RegisteredBankAccountMapper;
import com.mypay.banking.adapter.out.persistence.BankAccountJpaEntity;
import com.mypay.banking.application.port.in.RegisterBankAccountCommand;
import com.mypay.banking.application.port.in.RegisterBankAccountUseCase;
import com.mypay.banking.application.port.out.GetMembershipPort;
import com.mypay.banking.application.port.out.MembershipStatus;
import com.mypay.banking.application.port.out.RegisterBankAccountPort;
import com.mypay.banking.application.port.out.RequestBankAccountInfoPort;
import com.mypay.banking.domain.RegisteredBankAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class RegisterBankAccountService implements RegisterBankAccountUseCase {

    private final RegisterBankAccountPort registerBankAccountPort;
    private final RequestBankAccountInfoPort requestBankAccountInfoPort;
    private final GetMembershipPort getMembershipPort;

    @Override
    public RegisteredBankAccount registerBankAccount(RegisterBankAccountCommand registerBankAccountCommand) {

        MembershipStatus membershipStatus = getMembershipPort.getMembership(registerBankAccountCommand.getMembershipId());
        if(!membershipStatus.isValid()) {
            return null;
        }

        BankAccount bankAccountInfo = requestBankAccountInfoPort.getBankAccountInfo(
                GetBankAccountRequest.builder()
                        .bankName(registerBankAccountCommand.getBankId())
                        .BankAccountNumber(registerBankAccountCommand.getBankAccountNumber())
                        .isValid(registerBankAccountCommand.isLinkedStatusIsValid())
                        .build()
        );

        if(bankAccountInfo.isValid()){
            RegisteredBankAccount registeredBankAccount = RegisteredBankAccount.builder()
                    .registeredBankAccountId(registerBankAccountCommand.getRegisteredBankAccountId())
                    .membershipId(registerBankAccountCommand.getMembershipId())
                    .bankId(registerBankAccountCommand.getBankId())
                    .bankAccountNumber(registerBankAccountCommand.getBankAccountNumber())
                    .linkedStatusIsValid(registerBankAccountCommand.isLinkedStatusIsValid())
                    .build();
            BankAccountJpaEntity bankAccountJpaEntity = registerBankAccountPort.registerBankAccount(registeredBankAccount);
            return RegisteredBankAccountMapper.mapToDomainEntity(bankAccountJpaEntity);
        }else{
            return null;
        }
    }
}
