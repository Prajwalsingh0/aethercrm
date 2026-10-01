package com.aethercrm.sales;

import com.aethercrm.tenant.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OpportunityService {

    private final OpportunityRepository opportunityRepository;
    private final PipelineStageRepository stageRepository;
    private final StageHistoryRepository stageHistoryRepository;

    public OpportunityService(OpportunityRepository opportunityRepository,
                              PipelineStageRepository stageRepository,
                              StageHistoryRepository stageHistoryRepository) {
        this.opportunityRepository = opportunityRepository;
        this.stageRepository = stageRepository;
        this.stageHistoryRepository = stageHistoryRepository;
    }

    @Transactional
    public void ensureDefaultStages() {
        UUID orgId = TenantContext.requireOrganizationId();
        if (stageRepository.countByOrganizationId(orgId) > 0) return;
        String[][] defaults = {
                {"Prospecting", "0", "false", "false", "#94a3b8"},
                {"Qualification", "20", "false", "false", "#60a5fa"},
                {"Proposal", "50", "false", "false", "#a78bfa"},
                {"Negotiation", "75", "false", "false", "#f59e0b"},
                {"Closed Won", "100", "true", "false", "#22c55e"},
                {"Closed Lost", "0", "false", "true", "#ef4444"}
        };
        int pos = 0;
        for (String[] d : defaults) {
            PipelineStage s = PipelineStage.builder()
                    .name(d[0]).position(pos++).probability(Integer.parseInt(d[1]))
                    .won(Boolean.parseBoolean(d[2])).lost(Boolean.parseBoolean(d[3])).color(d[4]).build();
            s.setOrganizationId(orgId);
            stageRepository.save(s);
        }
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listStages() {
        ensureDefaultStages();
        UUID orgId = TenantContext.requireOrganizationId();
        return stageRepository.findByOrganizationIdOrderByPositionAsc(orgId).stream()
                .map(s -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", s.getId()); m.put("name", s.getName());
                    m.put("position", s.getPosition()); m.put("probability", s.getProbability());
                    m.put("won", s.isWon()); m.put("lost", s.isLost()); m.put("color", s.getColor());
                    return m;
                }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<Map<String, Object>> search(String status, UUID stageId, String q, Pageable pageable) {
        UUID orgId = TenantContext.requireOrganizationId();
        String query = (q != null && !q.isBlank()) ? q.trim() : null;
        return opportunityRepository.search(orgId, status, stageId, query, pageable).map(this::toMap);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> get(UUID id) {
        return toMap(findOwned(id));
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> body) {
        ensureDefaultStages();
        UUID orgId = TenantContext.requireOrganizationId();
        UUID userId = TenantContext.requireUserId();
        List<PipelineStage> stages = stageRepository.findByOrganizationIdOrderByPositionAsc(orgId);
        if (stages.isEmpty()) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No pipeline stages");
        UUID stageId = body.get("stageId") != null ? UUID.fromString(body.get("stageId").toString()) : stages.get(0).getId();
        PipelineStage stage = stageRepository.findByIdAndOrganizationId(stageId, orgId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid stage"));

        Opportunity o = Opportunity.builder()
                .name((String) body.getOrDefault("name", "Untitled Opportunity"))
                .accountId(parseUuid(body.get("accountId"))).contactId(parseUuid(body.get("contactId")))
                .stageId(stage.getId()).amount(parseDecimal(body.get("amount")))
                .currency(body.get("currency") != null ? body.get("currency").toString() : "INR")
                .probability(stage.getProbability()).expectedCloseDate(parseDate(body.get("expectedCloseDate")))
                .ownerId(parseUuid(body.get("ownerId")) != null ? parseUuid(body.get("ownerId")) : userId)
                .source(body.get("source") != null ? body.get("source").toString() : null)
                .description(body.get("description") != null ? body.get("description").toString() : null)
                .status("OPEN").createdBy(userId).updatedBy(userId).build();
        o.setOrganizationId(orgId);
        opportunityRepository.save(o);

        stageHistoryRepository.save(StageHistory.builder()
                .opportunityId(o.getId()).fromStageId(null).toStageId(stage.getId())
                .changedBy(userId).note("Created").build());
        return toMap(o);
    }

    @Transactional
    public Map<String, Object> moveStage(UUID opportunityId, UUID toStageId, String note) {
        Opportunity o = findOwned(opportunityId);
        UUID orgId = TenantContext.requireOrganizationId();
        UUID userId = TenantContext.requireUserId();
        PipelineStage toStage = stageRepository.findByIdAndOrganizationId(toStageId, orgId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid stage"));

        UUID fromStageId = o.getStageId();
        o.setStageId(toStage.getId());
        o.setProbability(toStage.getProbability());
        o.setUpdatedBy(userId);
        if (toStage.isWon()) { o.setStatus("WON"); o.setActualCloseDate(LocalDate.now()); }
        else if (toStage.isLost()) { o.setStatus("LOST"); o.setActualCloseDate(LocalDate.now()); }
        else { o.setStatus("OPEN"); o.setActualCloseDate(null); }
        opportunityRepository.save(o);

        stageHistoryRepository.save(StageHistory.builder()
                .opportunityId(o.getId()).fromStageId(fromStageId).toStageId(toStage.getId())
                .changedBy(userId).note(note).build());
        return toMap(o);
    }

    @Transactional
    public void softDelete(UUID id) {
        Opportunity o = findOwned(id);
        o.setDeletedAt(Instant.now());
        o.setUpdatedBy(TenantContext.requireUserId());
        opportunityRepository.save(o);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> pipelineSummary() {
        UUID orgId = TenantContext.requireOrganizationId();
        List<Opportunity> open = opportunityRepository.findByOrganizationIdAndDeletedAtIsNullAndStatus(orgId, "OPEN");
        BigDecimal openValue = open.stream().map(o -> o.getAmount() != null ? o.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal weighted = open.stream().map(o -> {
            BigDecimal amt = o.getAmount() != null ? o.getAmount() : BigDecimal.ZERO;
            int p = o.getProbability() != null ? o.getProbability() : 0;
            return amt.multiply(BigDecimal.valueOf(p)).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
        return Map.of("openDeals", open.size(), "openValue", openValue, "weightedValue", weighted);
    }

    private Opportunity findOwned(UUID id) {
        return opportunityRepository.findByIdAndOrganizationIdAndDeletedAtIsNull(id, TenantContext.requireOrganizationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Opportunity not found"));
    }

    private Map<String, Object> toMap(Opportunity o) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", o.getId()); m.put("name", o.getName()); m.put("accountId", o.getAccountId());
        m.put("contactId", o.getContactId()); m.put("stageId", o.getStageId()); m.put("amount", o.getAmount());
        m.put("currency", o.getCurrency()); m.put("probability", o.getProbability());
        m.put("expectedCloseDate", o.getExpectedCloseDate()); m.put("actualCloseDate", o.getActualCloseDate());
        m.put("ownerId", o.getOwnerId()); m.put("source", o.getSource()); m.put("description", o.getDescription());
        m.put("status", o.getStatus()); m.put("winReason", o.getWinReason()); m.put("lossReason", o.getLossReason());
        m.put("createdAt", o.getCreatedAt()); m.put("updatedAt", o.getUpdatedAt());
        return m;
    }

    private static UUID parseUuid(Object v) { return v == null ? null : UUID.fromString(v.toString()); }
    private static BigDecimal parseDecimal(Object v) { return v == null ? BigDecimal.ZERO : new BigDecimal(v.toString()); }
    private static LocalDate parseDate(Object v) { return v == null ? null : LocalDate.parse(v.toString()); }
}
