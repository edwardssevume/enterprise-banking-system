package com.enterprisebank.customer.service;

import com.enterprisebank.customer.dto.CreateCustomerRequest;
import com.enterprisebank.customer.dto.CustomerResponse;
import com.enterprisebank.customer.entity.Customer;
import com.enterprisebank.customer.entity.CustomerStatus;
import com.enterprisebank.customer.exception.CustomerNotFoundException;
import com.enterprisebank.customer.exception.DuplicateCustomerException;
import com.enterprisebank.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerResponse createCustomer(
            CreateCustomerRequest request,
            Long authUserId
    ) {

        String normalizedEmail = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (customerRepository.existsByAuthUserId(authUserId)) {
            throw new DuplicateCustomerException(
                    "A customer profile already exists for this user"
            );
        }

        if (customerRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateCustomerException(
                    "Email is already associated with another customer"
            );
        }

        String customerNumber = generateCustomerNumber();

        Customer customer = Customer.builder()
                .customerNumber(customerNumber)
                .authUserId(authUserId)
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(normalizedEmail)
                .phoneNumber(request.getPhoneNumber().trim())
                .dateOfBirth(request.getDateOfBirth())
                .addressLine1(request.getAddressLine1().trim())
                .addressLine2(
                        normalizeOptional(request.getAddressLine2())
                )
                .city(request.getCity().trim())
                .province(request.getProvince().trim())
                .postalCode(request.getPostalCode().trim())
                .country(request.getCountry().trim())
                .status(CustomerStatus.ACTIVE)
                .build();

        Customer savedCustomer =
                customerRepository.save(customer);

        return mapToResponse(savedCustomer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {

        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer not found with ID: " + id
                ));

        return mapToResponse(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByAuthUserId(
            Long authUserId
    ) {

        Customer customer = customerRepository
                .findByAuthUserId(authUserId)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer profile not found"
                ));

        return mapToResponse(customer);
    }

    private String generateCustomerNumber() {

        String year = String.valueOf(Year.now().getValue());

        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10)
                .toUpperCase(Locale.ROOT);

        return "CUS-" + year + "-" + randomPart;
    }

    private String normalizeOptional(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private CustomerResponse mapToResponse(Customer customer) {

        return new CustomerResponse(
                customer.getId(),
                customer.getCustomerNumber(),
                customer.getAuthUserId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getDateOfBirth(),
                customer.getAddressLine1(),
                customer.getAddressLine2(),
                customer.getCity(),
                customer.getProvince(),
                customer.getPostalCode(),
                customer.getCountry(),
                customer.getStatus(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}