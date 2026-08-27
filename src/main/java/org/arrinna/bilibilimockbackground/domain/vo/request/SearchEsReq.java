package org.arrinna.bilibilimockbackground.domain.vo.request;

import lombok.Data;
import org.arrinna.bilibilimockbackground.common.PageRequest;

import java.io.Serializable;

@Data
public class SearchEsReq extends PageRequest implements Serializable {

    private String keyword;

    private Integer sortCode;
}
