package com.undraw.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author readpage
 * @date 2023-01-16 14:29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DateDTO {
    private LocalDateTime dateTime;
    private LocalDate date;
}
