package PosWeb.POS.domain.dto.MemberTime;

import PosWeb.POS.domain.MemberTime;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
public class MemberTimeDto {
    private Long id;
    private Long memberId;
    private String StringId;
    private String name;
    private String startTime;
    private String endTime;
    private Double time;
    private int rate;

    public MemberTimeDto(MemberTime mt) {
        id = mt.getId();
        memberId = mt.getMember().getId();
        StringId = mt.getMember().getStringId();
        name = mt.getMember().getName();
        startTime = formatDateTime(mt.getStartTime());
        endTime = mt.getEndTime() != null ? formatDateTime(mt.getEndTime()) : "";
        time = mt.getTime();
        rate = mt.getRate();
    }

    private String formatDateTime(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyy.MM.dd HH:mm:ss");
        return localDateTime.format(formatter);
    }
}
