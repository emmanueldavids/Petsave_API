package com.petsave.petsave.dto;

import java.time.LocalDateTime;

import com.petsave.petsave.Entity.Gender;
import com.petsave.petsave.Entity.PaymentStatus;

public class DonationResponse {
    private Long id;
    private String donorName;
    private Float amount;
    private LocalDateTime date;
    private Gender gender;
    private String country;
    private PaymentStatus paymentStatus;


    public DonationResponse(Long id, String donorName, Float amount, LocalDateTime date, Gender gender, String country, PaymentStatus paymentStatus) {
        this.id = id;
        this.donorName = donorName;
        this.amount = amount;
        this.date = date;
        this.gender = gender;
        this.country = country;
        this.paymentStatus = paymentStatus;
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
                '}';
    }
    
    public DonationResponse() {
        // Default constructor
    }
    public DonationResponse(String donorName, Float amount, LocalDateTime date, Gender gender, String country, PaymentStatus paymentStatus) {
        this.donorName = donorName;
        this.amount = amount;
        this.date = date;
        this.gender = gender;
        this.country = country;
        this.paymentStatus = PaymentStatus.PENDING; // Default status
    }

}
