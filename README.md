# 🧠 AI Job Matcher — System Overview

This project is composed of three key components that work together to provide AI-powered resume-to-job matching:

---

## 🔧 ai-job-matcher (Spring Boot Backend)

This is the **backend API layer** of the application. It handles:

- ✅ **REST API endpoints** for creating, retrieving, updating, and deleting job listings and resumes.
- ✅ **Business logic** and validation.
- ✅ **Database integration** for persistent storage of jobs and resumes.
- ✅ **AI communication**: It sends resume + job description data to the AI microservice (`ai-embed-matcher`) and receives similarity scores in return.

---

## 💻 ai-job-matcher-frontend (Next.js Frontend)

This is the **user-facing interface** of the system. It allows users to:

- 📄 **Upload resumes**
- 💼 **Browse job listings**
- 🤖 **View AI-powered match scores** between their resume and open jobs

The frontend communicates with the `ai-job-matcher` backend via HTTP requests.

---

## 🧠 ai-embed-matcher (Python Flask AI Microservice)

This is a **Python microservice** that performs semantic similarity scoring using a transformer model. It:

- 🧩 Loads the `all-MiniLM-L6-v2` model via the `sentence-transformers` library
- 🧮 Converts resume and job description text into **numerical embeddings**
- 📏 Computes **cosine similarity** between the two embeddings
- 🔁 Returns a similarity score between 0 and 1 to the Java backend

This component is responsible for the core **AI logic** of the platform.

---

## 🔗 How They Work Together

```
[Frontend UI (Next.js)]
        ↓
[Backend API (Spring Boot)] ←→ [Database]
        ↓
[AI Matching Engine (Flask + Sentence Transformers)]
```

---

## 💡 Summary

This system brings together modern full-stack development with applied AI to deliver:
- 🚀 Intelligent job-resume matching
- 🔗 Modular microservices architecture
- 🧱 Scalable backend + clean frontend + pluggable AI layer

Built with ❤️ by Darrien Johnson

