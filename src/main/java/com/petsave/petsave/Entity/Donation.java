package com.petsave.petsave.Entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;


@Entity
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    
    private Long id;
    private String donorName;
    private Float amount;
    private LocalDateTime date;
    private Gender gender;
    private String country;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    private String email; // required by Paystack
    private String reference; // Paystack transaction reference


    public Donation(
        Long id, 
        String donorName, 
        Float amount, 
        LocalDateTime date, 
        Gender gender, 
        String country, 
        String email, 
        String reference)
        {
            this.id = id;
            this.donorName = donorName;
            this.amount = amount;
            this.date = date;
            this.gender = gender;
            this.country = country;
            this.email = email;
            this.reference = reference;
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
    @Override
    public String toString() {
        return "Donation{" +
                "id='" + id + '\'' +
                ", donorName='" + donorName + '\'' +
                ", amount='" + amount + '\'' +
                ", date='" + date + '\'' +
                ", gender='" + gender + '\'' +
                ", country='" + country + '\'' +
                ", email='" + email + '\'' +
                ", reference='" + reference + '\'' +
                '}';
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Donation)) return false;

        Donation donation = (Donation) o;

        if (!id.equals(donation.id)) return false;
        if (!donorName.equals(donation.donorName)) return false;
        if (!amount.equals(donation.amount)) return false;
        if (!date.equals(donation.date)) return false;
        if (!gender.equals(donation.gender)) return false;
        if (!country.equals(donation.country)) return false;
        if (!email.equals(donation.email)) return false;
        return reference.equals(donation.reference);
    }
    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + donorName.hashCode();
        result = 31 * result + amount.hashCode();
        result = 31 * result + date.hashCode();
        result = 31 * result + gender.hashCode();
        result = 31 * result + country.hashCode();
        result = 31 * result + email.hashCode();
        result = 31 * result + reference.hashCode();
        return result;
    }
    public Donation() {
        // Default constructor for serialization/deserialization    
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

}
