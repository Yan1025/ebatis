package com.ymm.ebatis.response;

import com.ymm.ebatis.annotation.Agg;
import com.ymm.ebatis.annotation.AggType;
import com.ymm.ebatis.annotation.Metric;
import com.ymm.ebatis.annotation.MetricType;
import com.ymm.ebatis.common.DslUtils;
import com.ymm.ebatis.meta.MethodMeta;
import com.ymm.ebatis.meta.RequestType;
import com.ymm.ebatis.meta.ResultType;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.springframework.core.ResolvableType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author 章多亮
 * @since 2020/1/17 16:34
 */
public abstract class AbstractAggResponseExtractorProvider extends AbstractResponseExtractorProvider {
    private static final Map<AggType, Function<MethodMeta, SearchResponseExtractor<?>>> RESPONSE_EXTRACTORS = new HashMap<>();

    static {
        RESPONSE_EXTRACTORS.put(AggType.METRIC, AbstractAggResponseExtractorProvider::createMetricAggSearchResponseExtractor);
    }

    protected AbstractAggResponseExtractorProvider(ResultType... resultTypes) {
        super(RequestType.AGG, resultTypes);
    }

    private static SearchResponseExtractor<?> createMetricAggSearchResponseExtractor(MethodMeta method) {
        Metric metric = DslUtils.getFirstElementRequired(method.getAnnotation(Agg.class).metric());

        MetricType type = metric.type();

        switch (type) {
            case VALUE_COUNT:
                return ValueCountAggResponseExtractor.INSTANCE;
            case AVG:
                return AgvAggResponseExtractor.INSTANCE;
            case MAX:
                return MaxAggResponseExtractor.INSTANCE;
            case MIN:
                return MinAggResponseExtractor.INSTANCE;
            case SUM:
                return SumAggResponseExtractor.INSTANCE;
            default:
                throw new UnsupportedOperationException();
        }
    }


    @Override
    protected ResponseExtractor<?> getResponseExtractor(ResolvableType resolvedResultType) {
        Class<?> resultClass = resolvedResultType.resolve();

        if (SearchResponse.class == resultClass) {
            return RawSearchResponseExtractor.INSTANCE;
        } else if (Aggregations.class == resultClass) {
            return AggregationsResponseExtractor.INSTANCE;
        } else if (List.class.isAssignableFrom(resultClass)) {
            if (Aggregation.class == resolvedResultType.resolveGeneric(0)) {
                return AggregationListResponseExtractor.INSTANCE;
            }
        } else if (Map.class.isAssignableFrom(resultClass)) {
            Class<?> keyClass = resolvedResultType.resolveGeneric(0);
            Class<?> valueClass = resolvedResultType.resolveGeneric(1);

            if (String.class == keyClass && Aggregation.class == valueClass) {
                return AggregationMapResponseExtractor.INSTANCE;
            }
        }

        return null;
    }

    @Override
    public ResponseExtractor<?> getResponseExtractor(MethodMeta meta) {
        Agg agg = meta.getAnnotation(Agg.class);
        AggType type = agg.type();

        return RESPONSE_EXTRACTORS.get(type).apply(meta);
    }
}