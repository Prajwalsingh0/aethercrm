package com.aethercrm.sales;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StageHistoryRepository extends JpaRepository<StageHistory, UUID> {
    List<StageHistory> findByOpportunityIdOrderByChangedAtDesc(UUID opportunityId);
}
