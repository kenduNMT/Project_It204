package bk.entity;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "application")
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitment_position_id")
    private RecruitmentPosition recruitmentPosition;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private Status status; // PENDING, INTERVIEWING, APPROVED, REJECTED, DESTROYED, DONE

    @Column(name = "interview_time")
    private LocalDateTime interviewTime;

    @Column(name = "interview_result")
    private String interviewResult;

    @Column(name = "interview_result_note")
    private String interviewResultNote;

    @Column(name = "destroy_reason")
    private String destroyReason;

    public enum Status {
        PENDING, INTERVIEWING, APPROVED, REJECTED, DESTROYED, DONE
    }
}