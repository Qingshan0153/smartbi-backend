package com.bi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bi.mapper.ChartMapper;
import com.bi.model.entity.Chart;
import com.bi.service.ChartService;
import org.springframework.stereotype.Service;

/**
* @author mendax
* @description 针对表【chart(图标信息表)】的数据库操作Service实现
* @createDate 2023-09-21 21:01:08
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService {

}




