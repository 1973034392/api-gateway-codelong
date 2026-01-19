package lua

const SlidingWindowScript = `
local key = KEYS[1]
local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local current = tonumber(ARGV[3])
local expire_time = current - window * 1000
redis.call('zremrangebyscore', key, 0, expire_time)
local count = redis.call('zcard', key)
if count < limit then
    redis.call('zadd', key, current, current)
    redis.call('expire', key, window + 1)
    return 1
else
    return 0
end
`

const TokenBucketScript = `
local key = KEYS[1]
local limit = tonumber(ARGV[1])
local current = tonumber(redis.call('get', key) or '0')
if current < limit then
    redis.call('incr', key)
    if current == 0 then
        redis.call('expire', key, 1)
    end
    return 1
else
    return 0
end
`

const BatchGetTokensScript = `
local key = KEYS[1]
local batch_size = tonumber(ARGV[1])
local limit = tonumber(ARGV[2])
local current = tonumber(redis.call('get', key) or '0')
local available = math.min(batch_size, limit - current)
if available > 0 then
    redis.call('incrby', key, available)
    if current == 0 then
        redis.call('expire', key, 1)
    end
    return available
else
    return 0
end
`
