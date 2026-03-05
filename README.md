<h1>NR-Donation</h1>

<p>해당 프로젝트는 마인크래프트와 인터넷 방송(숲, 치지직, 유튜브, 투네이션, 위플랩) 연동을 해주는 플러그인 / 모드 프로젝트 입니다.</p>
<p>개발자 위키는 기본적으로 디스코드에서 확인 하실 수 있습니다.</p>
<p>해당 플러그인을 사용하기 위해선 치지직, 숲 공식 API Key가 필요합니다.</p>

<h2>모드 및 플러그인 버전</h2>

- 플러그인 : spigot 1.12+
  - 1.12.x~1.16.x -all 버전 사용
  - 1.17+ 일반 버전 사용
- 포지 : 1.12.2
- 패브릭 : 1.20.1+
  - 랜더러 0.17.3+

<h2>라이센스</h2>

Copyright (c) 2025 Teujaem

1. 상업적 이용이 가능합니다.
2. 2차 수정이 불가능 합니다. (fork 포함)
3. 2차 배포가 불가능 합니다.
   - 예외사항:
     - 런쳐나, 패치기 같은 형태로는 2차 배포가 가능합니다.

<h2>도움 주신 분</h2>

- 아포칼립스 (유튜브, 투네이션, 위플랩)
- Entry (1.12.2 forge)
- 한올 (1.21.1 neoforge)

<h2>디스코드</h2>
https://discord.gg/9WU269Rf5e

<h2>사용 방법</h2>
<p>직접 빌드 해서 사용하셔도 되고, 디스코드방에서 빌드된 리소스를 배포하고 있습니다.</p>
<p>직접 빌드해서 사용할 경우 유튜브, 투네이션, 위플랩 로컬 maven 필요합니다 ( Tasks/publishing/publishToMavenLocal )</p>
<p>하위버전 빌드시 gradle 8.14.3 사용하세요 ( gradle/wrapper/gradle-wrapper.properties )</p>
<p>settings.gradle, gradle.properties에 프로젝트 설정 있습니다</p>

<h2>참고</h2>
<p>기본적으로 Soop Node.js 서버는 제공되지 않습니다</p>
<p>만약 Soop 기능을 이용하고 싶을 시, 디스코드에서 문의 주시기 바립니다.</p>
<p>버그 제보는 디스코드 "teujaem"으로 제보 부탁드립니다</p>
<p>위플랩, 유튜브는 수정중에 있습니다</p>

<h2>config</h2>

<p>.minecraft/config/NRDonationConfig.yml</p>

```
# 실제 마인크래프트 서버가 아닌, 후원 연동을 위한 서버
#
Server:
  ip: "127.0.0.1"
  port: 8888

#
# 이벤트 화이트리스트 기능
#
SendEvent:
  donation: true
  chat: true

Url:
  # 생방송 링크
  youtube: ""
  # 알림창 Url
  toonation: ""
  # 알림창 Url
  weflab: ""

API:
  # YouTube Data API v3 API
  youtube: ""
```

<p>bukkit/plugins/NR-Donation/config.yml</p>

```
# type은 "main" or "sub"중 하나만 선택 해주세요
# 서버가 하나일 경우, "main"을 입력해주세요
#
Server:
  type: "main"
  host: "0.0.0.0"
  port: 8888

#
# 숲, 치지직의 Open API Key를 입력하는 곳 입니다.
#
APIKey:
  Soop:
    id: "id"
    secret: "secret"
  Chzzk:
    id: "id"
    secret: "secret"

#
# 중간 API Server가 있는 플랫폼 API Server 주소 설정
#
APIServer:
  Soop:
    nodejs: "localhost:3000"
```