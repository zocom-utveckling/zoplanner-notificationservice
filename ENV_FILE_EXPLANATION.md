# ⚠VIKTIGT: .env vs .env.example

## Skillnaden mellan filerna:

### `.env.example` (Template/Mall)
- ✅ **Committas till Git** (finns i repot)
- ✅ Visar vilka environment variables som behövs
- ✅ Innehåller exempel-värden
- ❌ **Används INTE av Docker**
- ❌ Innehåller inga riktiga credentials

### `.env` (Din faktiska konfiguration)
- ❌ **Committas INTE till Git** (finns i .gitignore)
- ✅ **Används av Docker** (docker-compose läser denna fil)
- ✅ Innehåller dina riktiga AWS-credentials
- ⚠️ Ska ALDRIG delas eller committas (innehåller hemligheter)

## Behövs båda filerna?

**JA! Båda filerna behövs av olika anledningar:**

| Fil | Syfte | Finns i Git? | Används av Docker? |
|-----|-------|--------------|-------------------|
| `.env.example` | Template/dokumentation | ✅ Ja | ❌ Nej |
| `.env` | Faktiska credentials | ❌ Nej (gitignored) | ✅ Ja |

### Vad händer om jag bara har en av dem?

**Endast `.env.example`:**
- ❌ Docker kan inte starta (letar efter `.env`)
- ❌ Måste manuellt kopiera till `.env` först
- ❌ docker-compose up kommer att misslyckas

**Endast `.env`:**
- ❌ Committas inte till Git (gitignored)
- ❌ Nya utvecklare vet inte vilka variabler som behövs
- ❌ Ingen dokumentation om konfiguration
- ❌ Teamet kan inte sätta upp projektet

**Båda filerna (RÄTT):**
- ✅ `.env.example` i Git → andra ser vad som behövs
- ✅ `.env` lokalt → Docker fungerar
- ✅ Säkert → credentials committas aldrig

## Hur det fungerar:

```
.env.example (mall i Git)
     ↓
     Kopieras av utvecklare
     ↓
.env (lokal fil, gitignored)
     ↓
     Läses av Docker
     ↓
Spring Boot Application
```

## Vad jag har gjort:

Jag har skapat `.env` filen genom att kopiera `.env.example`:
```bash
Copy-Item .env.example .env
```

## 🔐 Nästa steg - VIKTIGT!

**Redigera `.env` filen med dina riktiga AWS-credentials:**

```env
# AWS SES Configuration
AWS_REGION=eu-north-1
AWS_ACCESS_KEY_ID=din_riktiga_access_key_här
AWS_SECRET_ACCESS_KEY=din_riktiga_secret_key_här
SES_FROM_EMAIL=din-faktiska-email@example.com

# Application Configuration
SERVER_PORT=8080
```

## SÄKERHET:

1. **ALDRIG** commita `.env` till Git
2. **Verifiera** att `.env` finns i `.gitignore` ✅ (redan fixat)
3. **Dela ALDRIG** `.env` filen med andra
4. **Använd** `.env.example` som mall för nya utvecklare

## För teammedlemmar:

När en ny utvecklare börjar:
```bash
# Steg 1: Klona repot
git clone ...

# Steg 2: Kopiera template
cp .env.example .env

# Steg 3: Fyll i egna credentials i .env
# (Varje utvecklare använder sina egna AWS-keys)
```

## Testa att det fungerar:

```bash
# 1. Verifiera att .env finns
Test-Path .env  # Should return: True

# 2. Bygg och starta Docker
mvn clean package -DskipTests
docker-compose up -d

# 3. Kontrollera att env variables laddas
docker exec zoplanner-notification-service printenv | grep AWS
```

## Checklista:

- [x] `.env.example` finns i Git (template)
- [x] `.env` skapad lokalt (från template)
- [x] `.env` finns i `.gitignore`
- [ ] Redigera `.env` med riktiga AWS-credentials
- [ ] Testa att Docker läser `.env` korrekt

## 🤔 Vanliga frågor:

### Kan jag ta bort .env.example och bara ha .env?
❌ **Nej** - då får nya utvecklare ingen dokumentation om vilka variabler som behövs.

### Kan jag commita .env till Git istället för .env.example?
❌ **ABSOLUT INTE** - då läcker dina AWS-credentials till alla som har tillgång till repot!

### Kan jag använda .env.example direkt i docker-compose.yml?
❌ **Nej** - då måste du commita riktiga credentials till Git (säkerhetsrisk!).

### Vad händer om jag glömmer skapa .env?
```bash
docker-compose up -d
# ERROR: .env file not found
# eller
# Applikationen startar men använder default-värden (kan misslyckas)
```

### Hur vet jag om .env är gitignored?
```bash
# Kontrollera i .gitignore
cat .gitignore | grep .env

# Testa om Git trackar filen
git status .env
# Bör säga: "not tracked" eller ingen output
```

### Kan andra utvecklare se min .env fil?
❌ **Nej** - den finns bara på din dator och committas aldrig till Git (om .gitignore är korrekt).

---

## Sammanfattning:

✅ **Behåll BÅDA filerna:**
- `.env.example` → Dokumentation/template (i Git)
- `.env` → Dina faktiska credentials (INTE i Git)

✅ **Detta är branschstandard** för alla projekt med hemligheter!

---

**Nu är du redo att köra Docker! Men glöm inte att fylla i dina riktiga AWS-credentials i `.env` filen först!** 🚀

