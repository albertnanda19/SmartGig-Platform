package com.smartgig.user.mapper;

import com.smartgig.user.dto.response.SkillResponse;
import com.smartgig.user.entity.Skill;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-02T01:47:34+0700",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.2 (Homebrew)"
)
@Component
public class SkillMapperImpl implements SkillMapper {

    @Override
    public SkillResponse toResponse(Skill entity) {
        if ( entity == null ) {
            return null;
        }

        SkillResponse.SkillResponseBuilder skillResponse = SkillResponse.builder();

        skillResponse.id( entity.getId() );
        skillResponse.name( entity.getName() );
        skillResponse.slug( entity.getSlug() );
        skillResponse.description( entity.getDescription() );
        skillResponse.iconUrl( entity.getIconUrl() );
        skillResponse.usageCount( entity.getUsageCount() );

        skillResponse.category( entity.getCategory().name() );

        return skillResponse.build();
    }
}
