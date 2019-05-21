package org.lenskit.mooc.cbf;

import org.lenskit.data.ratings.Rating;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Build a user profile from all positive ratings.
 */
public class WeightedUserProfileBuilder implements UserProfileBuilder {
    /**
     * The tag model, to get item tag vectors.
     */
    private final TFIDFModel model;
    private static final double RATING_THRESHOLD = 3.5;
    private static final Logger logger = LoggerFactory.getLogger(TFIDFModelProvider.class);

    @Inject
    public WeightedUserProfileBuilder(TFIDFModel m) {
        model = m;
    }

    @Override
    public Map<String, Double> makeUserProfile(@Nonnull List<Rating> ratings) {
        // Create a new vector over tags to accumulate the user profile
        Map<String,Double> profile = new HashMap<>();

        // TODO Normalize the user's ratings
        // TODO Build the user's weighted profile
        Double mean = 0.0;
        for (Rating r: ratings) {
            mean += r.getValue();

        }
        mean = mean / ratings.size();
        logger.debug("Mean: " + mean);

            for (Rating r: ratings) {

                // TODO Get this item's vector and add it to the user's profile

                Map<String, Double> itemMap = model.getItemVector(r.getItemId()) ;
                for(Map.Entry<String, Double> e: itemMap.entrySet()) {
                    if (!profile.containsKey(e.getKey()))
                        profile.put(e.getKey(), 0.0);

                    profile.put(e.getKey(), profile.get(e.getKey()) + (r.getValue() - mean) * e.getValue() );
                }
            }


        // The profile is accumulated, return it.
        return profile;
    }
}
