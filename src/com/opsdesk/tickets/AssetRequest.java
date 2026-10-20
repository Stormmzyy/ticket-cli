package com.opsdesk.tickets;

/**
 * A request for a physical or digital asset — e.g. a laptop, a license key.
 *
 * This is the second subclass of Ticket, showing that different
 * ticket types can carry different data while still being treated
 * as plain Tickets when you don't care about the specifics.
 */
public class AssetRequest extends Ticket {

    // What kind of asset is being requested?
    // e.g. "laptop", "monitor", "software license"
    private String assetType;

    public AssetRequest(String id, String title, String status, int priority, String assetType) {
        super(id, title, status, priority);
        this.assetType = assetType;
    }

    @Override
    public String describe() {
        return String.format("[%s] ASSET: %s (status: %s, priority: %d, asset: %s)",
                id, title, status, priority, assetType);
    }

    public String getAssetType() {
        return assetType;
    }
}
