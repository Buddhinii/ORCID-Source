package org.orcid.cron;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.orcid.listener.persistence.entities.Api30RecordStatusEntity;
import org.orcid.listener.persistence.managers.Api30RecordStatusManager;
import org.orcid.listener.persistence.util.ActivityType;
import org.orcid.listener.s3.S3MessageProcessorAPIV3;
import org.orcid.listener.util.SystemAlertsUtil;
import org.orcid.utils.listener.RetryMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class ResendV3FailedMessages {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResendV3FailedMessages.class);
    private static final int BATCH_SIZE = 1000;
    
    @Value("${org.orcid.message-listener.retry:5}")
    private Integer maxFailuresBeforeNotify;
    
    @Resource
    private Api30RecordStatusManager api30RecordStatusManager;
    
    @Resource
    S3MessageProcessorAPIV3 proc;
    
    @Resource 
    private SystemAlertsUtil systemAlertsUtil;
    
    @Scheduled(cron = "${org.orcid.cron.v3.reindex-failed:0 0 7,13 * * *}")
    public void resendFailedElements() {
        LOGGER.info("Processing failed elements for V3.0");
        List<ActivityType> retryList = new ArrayList<ActivityType>();
        // Get elements that failed
        List<Api30RecordStatusEntity> failedElements = api30RecordStatusManager.getFailedElements(BATCH_SIZE);
        List<Api30RecordStatusEntity> elementsToNotify = new ArrayList<Api30RecordStatusEntity>();
        
        for (Api30RecordStatusEntity element : failedElements) {
            boolean summaryFailed = false;
            
            if(element.getSummaryStatus() > 0) {
                summaryFailed = true;
                if(element.getSummaryStatus() > maxFailuresBeforeNotify) {
                    elementsToNotify.add(element);
                }
            }
            
            if(element.getDistinctionsStatus() > 0) {
                retryList.add(ActivityType.DISTINCTIONS);
                if(element.getDistinctionsStatus() > maxFailuresBeforeNotify) {
                    elementsToNotify.add(element);
                }
            }
            
            if(element.getEducationsStatus() > 0) {
                retryList.add(ActivityType.EDUCATIONS);
                if(element.getEducationsStatus() > maxFailuresBeforeNotify) {
                    elementsToNotify.add(element);
                }
            }
            
            if(element.getEmploymentsStatus() > 0) {
                retryList.add(ActivityType.EMPLOYMENTS);
                if(element.getEmploymentsStatus() > maxFailuresBeforeNotify) {
                    elementsToNotify.add(element);
                }
            }
            
            if(element.getFundingsStatus() > 0) {
                retryList.add(ActivityType.FUNDINGS);
                if(element.getFundingsStatus() > maxFailuresBeforeNotify) {
                    elementsToNotify.add(element);
                }
            }
            
            if(element.getInvitedPositionsStatus() > 0) {
                retryList.add(ActivityType.INVITED_POSITIONS);
                if(element.getInvitedPositionsStatus() > maxFailuresBeforeNotify) {
                    elementsToNotify.add(element);
                }
            }
            
            if(element.getMembershipStatus() > 0) {
                retryList.add(ActivityType.MEMBERSHIP);
                if(element.getMembershipStatus() > maxFailuresBeforeNotify) {
                    elementsToNotify.add(element);
                }
            }
            
            if(element.getPeerReviewsStatus() > 0) {
                retryList.add(ActivityType.PEER_REVIEWS);
                if(element.getPeerReviewsStatus() > maxFailuresBeforeNotify) {
                    elementsToNotify.add(element);
                }
            }
            
            if(element.getQualificationsStatus() > 0) {
                retryList.add(ActivityType.QUALIFICATIONS);
                if(element.getQualificationsStatus() > maxFailuresBeforeNotify) {
                    elementsToNotify.add(element);
                }
            }
            
            if(element.getResearchResourcesStatus() > 0) {
                retryList.add(ActivityType.RESEARCH_RESOURCES);
                if(element.getResearchResourcesStatus() > maxFailuresBeforeNotify) {
                    elementsToNotify.add(element);
                }
            }
            
            if(element.getServicesStatus() > 0) {
                retryList.add(ActivityType.SERVICES);
                if(element.getServicesStatus() > maxFailuresBeforeNotify) {
                    elementsToNotify.add(element);
                }
            }
            
            if(element.getWorksStatus() > 0) {
                retryList.add(ActivityType.WORKS);
                if(element.getWorksStatus() > maxFailuresBeforeNotify) {
                    elementsToNotify.add(element);
                }
            }
            
            RetryMessage message = new RetryMessage(element.getId(), summaryFailed);
            proc.retry(message, summaryFailed, retryList);
        }

        // Send summary
        if (!elementsToNotify.isEmpty()) {
            String message = buildAlertMessage(elementsToNotify);
            systemAlertsUtil.sendSystemAlert(message);
        }
    }
    
    private String buildAlertMessage(List<Api30RecordStatusEntity> elements) {
        StringBuilder sb = new StringBuilder("The following records failed to be processed in the message listener: ");
        sb.append(System.lineSeparator() + System.lineSeparator());

        for (Api30RecordStatusEntity element : elements) {
            sb.append("*ORCID: '").append(element.getId()).append("':* ");

            if(element.getSummaryStatus() > 0) {
                sb.append(" (3.0 Summary: ");
                sb.append(element.getSummaryStatus());
                sb.append(" failures)");
            }
            
            if(element.getDistinctionsStatus() > 0) {
                sb.append(" (3.0 Distinctions: ");
                sb.append(element.getDistinctionsStatus());
                sb.append(" failures)");
            }
            
            if(element.getEducationsStatus() > 0) {
                sb.append(" (3.0 Educations: ");
                sb.append(element.getEducationsStatus());
                sb.append(" failures)");
            }
            
            if(element.getEmploymentsStatus() > 0) {
                sb.append(" (3.0 Employments: ");
                sb.append(element.getEmploymentsStatus());
                sb.append(" failures)");
            }
            
            if(element.getFundingsStatus() > 0) {
                sb.append(" (3.0 Fundings: ");
                sb.append(element.getFundingsStatus());
                sb.append(" failures)");
            }
            
            if(element.getInvitedPositionsStatus() > 0) {
                sb.append(" (3.0 Invited Positions: ");
                sb.append(element.getInvitedPositionsStatus());
                sb.append(" failures)");
            }
            
            if(element.getMembershipStatus() > 0) {
                sb.append(" (3.0 Memberships: ");
                sb.append(element.getMembershipStatus());
                sb.append(" failures)");
            }
            
            if(element.getPeerReviewsStatus() > 0) {
                sb.append(" (3.0 Peer Reviews: ");
                sb.append(element.getPeerReviewsStatus());
                sb.append(" failures)");
            }
            
            if(element.getQualificationsStatus() > 0) {
                sb.append(" (3.0 Qualifications: ");
                sb.append(element.getQualificationsStatus());
                sb.append(" failures)");
            }
            
            if(element.getResearchResourcesStatus() > 0) {
                sb.append(" (3.0 Research Resources: ");
                sb.append(element.getResearchResourcesStatus());
                sb.append(" failures)");
            }
            
            if(element.getServicesStatus() > 0) {
                sb.append(" (3.0 Services: ");
                sb.append(element.getServicesStatus());
                sb.append(" failures)");
            }
            
            if(element.getWorksStatus() > 0) {
                sb.append(" (3.0 Works: ");
                sb.append(element.getWorksStatus());
                sb.append(" failures)");
            }
            
            sb.append(System.lineSeparator());
        }

        return sb.toString();
    }
    
}
