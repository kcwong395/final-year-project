package com.fyp.shoemaker.model;

import com.github.javafaker.Faker;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.lang.NonNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.net.InetAddress;
import java.util.Locale;
import java.util.UUID;

@Entity
@Data
public class ShoeMaker {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;

    @Column(unique = true)
    private InetAddress ip;

    private String name;

    @NonNull
    private int port;

    private ShoeMaker.Status status;

    public ShoeMaker() {
    }

    public ShoeMaker(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
        name = new Faker(new Locale("en-GB")).name().fullName();
        this.status = Status.Active;
    }

    public enum Status {
        Active, Shutdown
    }
}
