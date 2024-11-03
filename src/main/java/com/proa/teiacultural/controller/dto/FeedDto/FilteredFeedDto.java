package com.proa.teiacultural.controller.dto.FeedDto;

import java.util.List;

public record FilteredFeedDto(List<FeedFilterDto> feedItems, int page, int pageSize, int totalPages, long totalElements) {
}