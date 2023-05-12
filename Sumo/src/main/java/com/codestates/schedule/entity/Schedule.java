package com.codestates.schedule.entity;

import com.codestates.member.entity.Member;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long scheduleId;

    @Column(nullable = false)
    private String date;

    @Column(nullable = false, name = "image_address")
    private String imageAddress;

    @Column
    private String memo;

    @Column
    private String location;

    @Column(nullable = false, name = "start_time")
    private String startTime;

    @Column(nullable = false, name = "end_time")
    private String endTime;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;
}
