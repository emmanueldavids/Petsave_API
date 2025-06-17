package com.petsave.petsave.dto;

import java.time.LocalDateTime;

import com.petsave.petsave.Entity.Gender;

public class DonationRequest {
    private String donorName;
    private Double amount;
    private LocalDateTime date;
    private Gender gender;
    private String country;
    private String email;
    private String reference;

    public DonationRequest(String donorName, Double amount, LocalDateTime date, Gender gender, String country, String email, String reference) {
        this.donorName = donorName;
        this.amount = amount;
        this.date = date;
        this.gender = gender;
        this.country = country;
        this.email = email;
        this.reference = reference;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
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
    @Override
    public String toString() {
        return "DonationRequest{" +
                "donorName='" + donorName + '\'' +
                ", amount=" + amount +
                ", date=" + date +
                ", gender=" + gender +
                ", country='" + country + '\'' +
                ", email='" + email + '\'' +
                ", reference='" + reference + '\'' +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DonationRequest)) return false;

        DonationRequest that = (DonationRequest) o;

        if (!donorName.equals(that.donorName)) return false;
        if (!amount.equals(that.amount)) return false;
        if (!date.equals(that.date)) return false;

        if (!gender.equals(that.gender)) return false;
        if (!country.equals(that.country)) return false;
        if (!email.equals(that.email)) return false;
        return reference.equals(that.reference);
    }
    @Override
    public int hashCode() {
        int result = donorName.hashCode();
        result = 31 * result + amount.hashCode();
        result = 31 * result + date.hashCode();
        result = 31 * result + gender.hashCode();
        result = 31 * result + country.hashCode();
        result = 31 * result + email.hashCode();
        result = 31 * result + reference.hashCode();
        return result;
    }
    public DonationRequest() {
        // Default constructor for serialization/deserialization
    }
}