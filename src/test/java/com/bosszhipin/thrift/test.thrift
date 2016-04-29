namespace java com.bosszhipin.thrift

struct TestReq {
    1: optional i32 param1,
    2: optional string param2
}

struct TestRes {
    1: optional i32 param1,
    2: optional string param2
}

service TestService {
    TestRes testMe(1: TestReq req)
    void ping()
}