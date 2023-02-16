rootProject.name = "redis-manager"

include("redis-manager-common")
project(":redis-manager-common").projectDir = file("Common")

include("redis-manager-jedis")
project(":redis-manager-jedis").projectDir = file("Jedis")

include("redis-manager-redisson")
project(":redis-manager-redisson").projectDir = file("Redisson")