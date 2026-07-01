package com.ttbt.smartclass.model.vo;

import java.io.Serializable;
import lombok.Data;

@Data
public class SpeechRecognizeVO implements Serializable {

    private String text;

    private static final long serialVersionUID = 1L;
}
