package com.petsave.petsave.dto;

import java.time.LocalDateTime;

import com.petsave.petsave.Entity.Gender;
import com.petsave.petsave.Entity.PaymentStatus;

import lombok.Data;

public class DonationResponse {
    private Long id;
    private String donorName;
    private Float amount;
    private LocalDateTime date;
    private Gender gender;
    private String country;
    private PaymentStatus paymentStatus;
    private String email;
    private String reference;

    private UserDto user;


    public DonationResponse(Long id, String donorName, Float amount, LocalDateTime date, Gender gender, String country, PaymentStatus paymentStatus, String email, String reference, UserDto user) {
        this.id = id;
        this.donorName = donorName;
        this.amount = amount;
        this.date = date;
        this.gender = gender;
        this.country = country;
        this.paymentStatus = paymentStatus;
        this.email = email;
        this.reference = reference;
        this.user = user;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getDonorName() {
        return donorName;
    }
    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }
    public Float getAmount() {
        return amount;
    }
    public void setAmount(Float amount) {
        this.amount = amount;
    }
    public LocalDateTime getDate() {
        return date;
    }
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    public Gender getGender() {
        return gender;
    }
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    public String getCountry() {
        return country;
    }
    public void setCountry(String country) {
        this.country = country;
    }
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getReference() {
        return reference;
    }
    public void setReference(String reference) {
        this.reference = reference;
    }
    public UserDto getUser() {
        return user;
    }
    public void setUser(UserDto user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "DonationResponse{" +
                "id=" + id +
                ", donorName='" + donorName + '\'' +
                ", amount=" + amount +
                ", date=" + date +
                ", gender=" + gender +
                " paymentStatus=" + paymentStatus +
                ", country='" + country + '\'' +
                ", email='" + email + '\'' +
                ", reference='" + reference + '\'' +
                '}';
    }
    
    public DonationResponse() {
        // Default constructor
    }
    // public DonationResponse(String donorName, Float amount, LocalDateTime date, Gender gender, String country, PaymentStatus paymentStatus, String email) {
    //     this.donorName = donorName;
    //     this.amount = amount;
    //     this.date = date;
    //     this.gender = gender;
    //     this.country = country;
    //     this.paymentStatus = PaymentStatus.PENDING; // Default status
    //     this.email = email;
    // }

    @Data
    public static class UserDto {
        private Long id;
        private String name;
        private String email;
        private String username;
    }

}
