package com.ymm.ebatis.builder;

import com.ymm.ebatis.annotation.MultiMatch;
import com.ymm.ebatis.exception.EbatisException;
import com.ymm.ebatis.meta.ConditionMeta;
import com.ymm.ebatis.provider.MultiMatchFieldProvider;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * @author 章多亮
 * @since 2020/1/16 18:59
 */
class MultiMatchQueryBuilderFactory extends AbstractQueryBuilderFactory<MultiMatchQueryBuilder, MultiMatch> {
    static final MultiMatchQueryBuilderFactory INSTANCE = new MultiMatchQueryBuilderFactory();

    private MultiMatchQueryBuilderFactory() {
    }

    @Override
    protected void setAnnotationMeta(MultiMatchQueryBuilder builder, MultiMatch multiMatch) {
        Float cutoffFrequency = multiMatch.cutoffFrequency() < 0 || multiMatch.cutoffFrequency() > 1 ? null : multiMatch.cutoffFrequency();

        builder.autoGenerateSynonymsPhraseQuery(multiMatch.autoGenerateSynonymsPhraseQuery())
                .cutoffFrequency(cutoffFrequency)
                .fuzziness(StringUtils.stripToNull(multiMatch.fuzziness()))
                .fuzzyRewrite(StringUtils.stripToNull(multiMatch.fuzzyRewrite()))
                .fuzzyTranspositions(multiMatch.fuzzyTranspositions())
                .lenient(multiMatch.lenient())
                .maxExpansions(multiMatch.maxExpansions())
                .prefixLength(multiMatch.prefixLength())
                .minimumShouldMatch(StringUtils.stripToNull(multiMatch.minimumShouldMatch()))
                .operator(multiMatch.operator())
                .slop(multiMatch.slop())
                .tieBreaker(multiMatch.tieBreaker());
    }

    @Override
    protected MultiMatchQueryBuilder doCreate(ConditionMeta meta, Object condition) {
        MultiMatch multiMatch = meta.findAttributeAnnotation(MultiMatch.class).orElseThrow(EbatisException::new);

        String[] fields = multiMatch.fields();

        if (condition instanceof MultiMatchFieldProvider) {
            fields = ArrayUtils.addAll(fields, ((MultiMatchFieldProvider) condition).getFields());
        }

        return QueryBuilders.multiMatchQuery(String.valueOf(condition), fields);
    }
}