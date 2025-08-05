package com.grewmeet.dating.datingcommandservice.testutil;

import com.grewmeet.dating.datingcommandservice.domain.DatingMeeting;
import com.grewmeet.dating.datingcommandservice.repository.DatingMeetingRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class InMemoryDatingMeetingRepository implements DatingMeetingRepository {

    private final Map<Long, DatingMeeting> storage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public DatingMeeting save(DatingMeeting entity) {
        if (entity.getId() == null) {
            // 새로운 엔티티의 경우 ID 할당 (리플렉션 사용)
            try {
                var idField = entity.getClass().getSuperclass().getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(entity, idGenerator.getAndIncrement());
            } catch (Exception e) {
                throw new RuntimeException("ID 설정 실패", e);
            }
        }
        storage.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public Optional<DatingMeeting> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public boolean existsById(Long id) {
        return storage.containsKey(id);
    }

    @Override
    public List<DatingMeeting> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<DatingMeeting> findAllById(Iterable<Long> ids) {
        List<DatingMeeting> result = new ArrayList<>();
        for (Long id : ids) {
            DatingMeeting meeting = storage.get(id);
            if (meeting != null) {
                result.add(meeting);
            }
        }
        return result;
    }

    @Override
    public long count() {
        return storage.size();
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }

    @Override
    public void delete(DatingMeeting entity) {
        storage.remove(entity.getId());
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        for (Long id : ids) {
            storage.remove(id);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends DatingMeeting> entities) {
        for (DatingMeeting entity : entities) {
            storage.remove(entity.getId());
        }
    }

    @Override
    public void deleteAll() {
        storage.clear();
    }

    @Override
    public List<DatingMeeting> findAll(Sort sort) {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Page<DatingMeeting> findAll(Pageable pageable) {
        throw new UnsupportedOperationException("Pageable not implemented in test");
    }

    @Override
    public <S extends DatingMeeting> List<S> saveAll(Iterable<S> entities) {
        List<S> result = new ArrayList<>();
        for (S entity : entities) {
            result.add((S) save(entity));
        }
        return result;
    }

    @Override
    public void flush() {
        // No-op for in-memory implementation
    }

    @Override
    public <S extends DatingMeeting> S saveAndFlush(S entity) {
        return (S) save(entity);
    }

    @Override
    public <S extends DatingMeeting> List<S> saveAllAndFlush(Iterable<S> entities) {
        return saveAll(entities);
    }

    @Override
    public void deleteAllInBatch(Iterable<DatingMeeting> entities) {
        deleteAll(entities);
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> ids) {
        deleteAllById(ids);
    }

    @Override
    public void deleteAllInBatch() {
        deleteAll();
    }

    @Override
    public DatingMeeting getOne(Long id) {
        return findById(id).orElseThrow();
    }

    @Override
    public DatingMeeting getById(Long id) {
        return findById(id).orElseThrow();
    }

    @Override
    public DatingMeeting getReferenceById(Long id) {
        return findById(id).orElseThrow();
    }

    @Override
    public <S extends DatingMeeting> Optional<S> findOne(Example<S> example) {
        throw new UnsupportedOperationException("Example queries not implemented in test");
    }

    @Override
    public <S extends DatingMeeting> List<S> findAll(Example<S> example) {
        throw new UnsupportedOperationException("Example queries not implemented in test");
    }

    @Override
    public <S extends DatingMeeting> List<S> findAll(Example<S> example, Sort sort) {
        throw new UnsupportedOperationException("Example queries not implemented in test");
    }

    @Override
    public <S extends DatingMeeting> Page<S> findAll(Example<S> example, Pageable pageable) {
        throw new UnsupportedOperationException("Example queries not implemented in test");
    }

    @Override
    public <S extends DatingMeeting> long count(Example<S> example) {
        throw new UnsupportedOperationException("Example queries not implemented in test");
    }

    @Override
    public <S extends DatingMeeting> boolean exists(Example<S> example) {
        throw new UnsupportedOperationException("Example queries not implemented in test");
    }

    @Override
    public <S extends DatingMeeting, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("FluentQuery not implemented in test");
    }

    // 테스트용 유틸리티 메서드
    public void clear() {
        storage.clear();
        idGenerator.set(1);
    }

    public int size() {
        return storage.size();
    }
}