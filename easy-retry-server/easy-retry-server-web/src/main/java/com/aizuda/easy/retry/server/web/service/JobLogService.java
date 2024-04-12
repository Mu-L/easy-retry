package com.aizuda.easy.retry.server.web.service;

import com.aizuda.easy.retry.server.web.model.base.PageResult;
import com.aizuda.easy.retry.server.web.model.request.JobLogQueryVO;
import com.aizuda.easy.retry.server.web.model.response.JobLogResponseVO;

import java.util.List;

/**
 * @author: opensnail
 * @date : 2023-10-12 09:54
 * @since ：2.4.0
 */
public interface JobLogService {

    JobLogResponseVO getJobLogPage(JobLogQueryVO jobQueryVO);
}
