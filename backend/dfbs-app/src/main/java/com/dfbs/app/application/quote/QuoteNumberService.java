package com.dfbs.app.application.quote;

import com.dfbs.app.modules.quote.QuoteSequenceEntity;
import com.dfbs.app.modules.quote.QuoteSequenceRepo;
import com.dfbs.app.modules.quote.enums.QuoteSourceType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class QuoteNumberService {

    private static final DateTimeFormatter YYMM = DateTimeFormatter.ofPattern("yyMM");
    private static final DateTimeFormatter YYMMDD = DateTimeFormatter.ofPattern("yyMMdd");

    private final QuoteSequenceRepo sequenceRepo;
    private final Clock clock;

    public QuoteNumberService(QuoteSequenceRepo sequenceRepo, Clock clock) {
        this.sequenceRepo = sequenceRepo;
        this.clock = clock != null ? clock : Clock.systemDefaultZone();
    }

    /**
     * Generates a unique quote number: Prefix + User + YYMMDD + 3-digit sequence.
     * Sequence is scoped by (userInitials, YYMM). Same user + same month -> seq increments;
     * same user + new month -> seq resets to 1; different users -> independent sequences.
     */
    @Transactional
    public synchronized String generate(QuoteSourceType type, String creatorName) {
        String prefix = type.getPrefix();
        String user = creatorName != null && !creatorName.isBlank() ? creatorName.trim() : "sys";
        LocalDate today = LocalDate.now(clock);
        String yearMonth = today.format(YYMM);
        String yearMonthDay = today.format(YYMMDD);

        QuoteSequenceEntity seqEntity = sequenceRepo.findByUserInitialsAndYearMonth(user, yearMonth)
                .orElse(null);

        int nextSeq;
        if (seqEntity != null) {
            nextSeq = seqEntity.getCurrentSeq() + 1;
            seqEntity.setCurrentSeq(nextSeq);
            sequenceRepo.save(seqEntity);
        } else {
            nextSeq = 1;
            QuoteSequenceEntity newEntity = new QuoteSequenceEntity();
            newEntity.setUserInitials(user);
            newEntity.setYearMonth(yearMonth);
            newEntity.setCurrentSeq(nextSeq);
            sequenceRepo.save(newEntity);
        }

        String seqPart = String.format("%03d", nextSeq);
        return prefix + user + yearMonthDay + seqPart;
    }
}
