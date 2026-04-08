package com.newproject.customer.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "customer_address")
public class Address {
    public static final String TYPE_SHIPPING = "SHIPPING";
    public static final String TYPE_BILLING = "BILLING";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "line_1", length = 255, nullable = false)
    private String line1;

    @Column(name = "line_2", length = 255)
    private String line2;

    @Column(length = 128, nullable = false)
    private String city;

    @Column(length = 128)
    private String region;

    @Column(length = 128, nullable = false)
    private String country;

    @Column(name = "postal_code", length = 32, nullable = false)
    private String postalCode;

    @Column(name = "address_type", length = 32, nullable = false)
    private String addressType;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public String getLine1() { return line1; }
    public void setLine1(String line1) { this.line1 = line1; }
    public String getLine2() { return line2; }
    public void setLine2(String line2) { this.line2 = line2; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getAddressType() { return addressType; }
    public void setAddressType(String addressType) { this.addressType = addressType; }
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
}
