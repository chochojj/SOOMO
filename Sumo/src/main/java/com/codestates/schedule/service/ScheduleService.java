package com.codestates.schedule.service;

import com.codestates.auth.LoginUtils;
import com.codestates.aws.S3Uploader;
import com.codestates.exception.BusinessLogicException;
import com.codestates.exception.ExceptionCode;
import com.codestates.member.entity.Member;
import com.codestates.member.repository.MemberRepository;
import com.codestates.schedule.entity.Schedule;
import com.codestates.schedule.repository.ScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final MemberRepository memberRepository;
    private final S3Uploader s3Uploader;

    public ScheduleService(ScheduleRepository scheduleRepository, MemberRepository memberRepository, S3Uploader s3Uploader) {
        this.scheduleRepository = scheduleRepository;
        this.memberRepository = memberRepository;
        this.s3Uploader = s3Uploader;
    }

    public Schedule createSchedule(Schedule schedule, MultipartFile image) throws IOException {

        String email = LoginUtils.checkLogin();
        Member member = memberRepository.findByEmail(email).get();
        schedule.setMember(member);

        // 중복 날짜 확인 로직
        verifyExistsDate(schedule.getDate(), member.getMemberId());

        verifyValidTimeSetting(schedule.getStartTime(), schedule.getEndTime());

        // durationTime 세팅 로직
        float durationTime = calculateDurationTime(schedule.getStartTime(), schedule.getEndTime());
        schedule.setDurationTime(durationTime);

        String imageAddress = s3Uploader.upload(image, member.getNickname());
        schedule.setImageAddress(imageAddress);

        return scheduleRepository.save(schedule);
    }

    public Schedule updateSchedule(Schedule schedule) {

        Schedule findSchedule = findVerifiedSchedule(schedule.getScheduleId());

        // 작성자와 수정자가 일치하는지 확인하는 로직
        String emailFromToken = LoginUtils.checkLogin();
        String emailFromSchedule = findSchedule.getMember().getEmail();
        checkAccessibleUser(emailFromToken, emailFromSchedule);

        Optional.ofNullable(schedule.getImageAddress()).ifPresent(imageAddress -> findSchedule.setImageAddress(imageAddress));
        Optional.ofNullable(schedule.getMemo()).ifPresent(memo -> findSchedule.setMemo(memo));
        Optional.ofNullable(schedule.getLocation()).ifPresent(location -> findSchedule.setLocation(location));
        Optional.ofNullable(schedule.getStartTime()).ifPresent(startTime -> findSchedule.setStartTime(startTime));
        Optional.ofNullable(schedule.getEndTime()).ifPresent(endTime -> findSchedule.setEndTime(endTime));

        // durationTime 세팅 로직
        float durationTime = calculateDurationTime(findSchedule.getStartTime(), findSchedule.getEndTime());
        findSchedule.setDurationTime(durationTime);

        return scheduleRepository.save(findSchedule);
    }

    public Schedule findSchedule(long scheduleId) {
        return findVerifiedSchedule(scheduleId);
    }

    private Schedule findVerifiedSchedule(long scheduleId) {
        Optional<Schedule> optionalSchedule = scheduleRepository.findById(scheduleId);
        Schedule schedule =
                optionalSchedule.orElseThrow(() -> new BusinessLogicException(ExceptionCode.SCHEDULE_NOT_FOUND));

        String emailFromToken = LoginUtils.checkLogin();
        String emailFromSchedule = schedule.getMember().getEmail();
        checkAccessibleUser(emailFromToken, emailFromSchedule);

        return schedule;
    }

    public List<Schedule> findOneUserSchedules(int year, int month) {
        String emailFromToken = LoginUtils.checkLogin();
        Member member = memberRepository.findByEmail(emailFromToken).get();
        List<Schedule> schedules = scheduleRepository.findByMember_MemberId(member.getMemberId());

        if (schedules.size() == 0) {
            return schedules;
        }

        Schedule findSchedule = schedules.get(0);
        String emailFromSchedule = findSchedule.getMember().getEmail();
        checkAccessibleUser(emailFromToken, emailFromSchedule);

        return schedules.stream()
                .filter(schedule -> schedule.getDate().getYear() == year
                        && schedule.getDate().getMonthValue() == month)
                .collect(Collectors.toList());
    }

    public List<List<Schedule>> findAllUsersSchedules(int year, int month) {
        String emailFromToken = LoginUtils.checkLogin();
        if (!emailFromToken.equals("SoomoAdmin@gmail.com")) {
            throw new BusinessLogicException(ExceptionCode.CANNOT_ACCESS_SCHEDULE);
        }

        List<Member> members = memberRepository.findAll();
        List<List<Schedule>> responses = new ArrayList<>();

        for (Member member : members) {
            List<Schedule> schedules = scheduleRepository.findByMember_MemberId(member.getMemberId());
            List<Schedule> filteredSchedules = schedules.stream()
                    .filter(schedule -> schedule.getDate().getYear() == year
                            && schedule.getDate().getMonthValue() == month)
                    .collect(Collectors.toList());
            responses.add(filteredSchedules);
        }

        return responses;
    }

    public void deleteSchedule(long scheduleId) {
        Schedule schedule = findVerifiedSchedule(scheduleId);
        scheduleRepository.delete(schedule);
    }

    private void checkAccessibleUser(String emailFromToken, String emailFromCalendar) {
        if (!emailFromToken.equals("SoomoAdmin@gmail.com") && !emailFromToken.equals(emailFromCalendar)) {
            throw new BusinessLogicException(ExceptionCode.CANNOT_ACCESS_SCHEDULE);
        }
    }

    private void verifyExistsDate(LocalDate date, long memberId) {
        Optional<Schedule> optionalSchedule = scheduleRepository.findByDateAndMember_MemberId(date, memberId);
        if (optionalSchedule.isPresent()) {
            throw new BusinessLogicException(ExceptionCode.SCHEDULE_EXISTS);
        }
    }

    private void verifyValidTimeSetting(LocalTime startTime, LocalTime endTime) {
        int compareTo = endTime.compareTo(startTime);
        if (compareTo <= 0) {
            throw new BusinessLogicException(ExceptionCode.INVALID_TIME_SETTING);
        }
    }

    private float calculateDurationTime(LocalTime startTime, LocalTime endTime) {
        Duration duration = Duration.between(startTime, endTime);
        float minutes = duration.toMinutes();
        float durationTime = minutes / 60;
        return durationTime;
    }
}
