package com.tejaslamba.vanillacore.social;

import java.util.UUID;

public class SocialPreferences {

    private final UUID uuid;
    private boolean chatEnabled;
    private boolean pmEnabled;
    private boolean mentionsEnabled;
    private boolean socialSpyEnabled;
    private UUID lastReplyTarget;

    public SocialPreferences(UUID uuid, boolean chatEnabled, boolean pmEnabled, boolean mentionsEnabled,
            boolean socialSpyEnabled, UUID lastReplyTarget) {
        this.uuid = uuid;
        this.chatEnabled = chatEnabled;
        this.pmEnabled = pmEnabled;
        this.mentionsEnabled = mentionsEnabled;
        this.socialSpyEnabled = socialSpyEnabled;
        this.lastReplyTarget = lastReplyTarget;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isChatEnabled() {
        return chatEnabled;
    }

    public void setChatEnabled(boolean chatEnabled) {
        this.chatEnabled = chatEnabled;
    }

    public boolean isPmEnabled() {
        return pmEnabled;
    }

    public void setPmEnabled(boolean pmEnabled) {
        this.pmEnabled = pmEnabled;
    }

    public boolean isMentionsEnabled() {
        return mentionsEnabled;
    }

    public void setMentionsEnabled(boolean mentionsEnabled) {
        this.mentionsEnabled = mentionsEnabled;
    }

    public boolean isSocialSpyEnabled() {
        return socialSpyEnabled;
    }

    public void setSocialSpyEnabled(boolean socialSpyEnabled) {
        this.socialSpyEnabled = socialSpyEnabled;
    }

    public UUID getLastReplyTarget() {
        return lastReplyTarget;
    }

    public void setLastReplyTarget(UUID lastReplyTarget) {
        this.lastReplyTarget = lastReplyTarget;
    }
}

