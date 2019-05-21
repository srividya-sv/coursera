package org.lenskit.mooc.cbf;

import org.lenskit.api.Result;
import org.lenskit.api.ResultMap;
import org.lenskit.basic.AbstractItemScorer;
import org.lenskit.data.dao.DataAccessObject;
import org.lenskit.data.entities.CommonAttributes;
import org.lenskit.data.ratings.Rating;
import org.lenskit.results.BasicResult;
import org.lenskit.results.Results;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="http://www.grouplens.org">GroupLens Research</a>
 */
public class TFIDFItemScorer extends AbstractItemScorer {
    private final DataAccessObject dao;
    private final TFIDFModel model;
    private final UserProfileBuilder profileBuilder;
    private static final Logger logger = LoggerFactory.getLogger(TFIDFModelProvider.class);

    /**
     * Construct a new item scorer.  LensKit's dependency injector will call this constructor and
     * provide the appropriate parameters.
     *
     * @param dao The data access object, for looking up users' ratings.
     * @param m   The precomputed model containing the item tag vectors.
     * @param upb The user profile builder for building user tag profiles.
     */
    @Inject
    public TFIDFItemScorer(DataAccessObject dao, TFIDFModel m, UserProfileBuilder upb) {
        this.dao = dao;
        model = m;
        profileBuilder = upb;
    }

    /**
     * Generate item scores personalized for a particular user.  For the TFIDF scorer, this will
     * prepare a user profile and compare it to item tag vectors to produce the score.
     *
     * @param user   The user to score for.
     * @param items  A collection of item ids that should be scored.
     */
    @Nonnull
    @Override
    public ResultMap scoreWithDetails(long user, @Nonnull Collection<Long> items){
        // Get the user's ratings
        List<Rating> ratings = dao.query(Rating.class)
                                  .withAttribute(CommonAttributes.USER_ID, user)
                                  .get();

        if (ratings == null) {
            // the user doesn't exist, so return an empty ResultMap
            return Results.newResultMap();
        }

        // Create a place to store the results of our score computations
        List<Result> results = new ArrayList<>();

        // Get the user's profile, which is a vector with their 'like' for each tag
        Map<String, Double> userVector = profileBuilder.makeUserProfile(ratings);

        Double totalUserProfile = 0.0;

        for(Map.Entry<String, Double> e: userVector.entrySet()) {
            totalUserProfile += (e.getValue() * e.getValue()) ;
        }

        for (Long item: items) {
            Map<String, Double> iv = model.getItemVector(item);

            if(iv == null)
                continue;


            Double total = 0.0;
            Double totalItemVector = 0.0;

            // TODO Compute the cosine of this item and the user's profile, store it in the output list


            for(Map.Entry<String, Double> tag: iv.entrySet()) {
                totalItemVector += (tag.getValue() * tag.getValue() );
                if(userVector.containsKey(tag.getKey())) {
                    total += (userVector.get(tag.getKey()) * tag.getValue());
                }
            }


            // If the denominator of the cosine similarity is 0, skip the item

            if(totalItemVector != 0 && totalUserProfile != 0) {
                Double cosineSim = total / Math.sqrt(totalItemVector) / Math.sqrt(totalUserProfile);
               if(item == 32 || item == 1748 || item == 1206)
                    logger.debug("Cosine sim for " + item + " = " + cosineSim + " total: " + total + " Item :" + totalItemVector + " User Profile: " + totalUserProfile);
                results.add(new BasicResult(item, cosineSim));
            }
            // TODO And remove this exception to say you've implemented it

           // throw new UnsupportedOperationException("stub implementation");
        }

        return Results.newResultMap(results);
    }
}
































































