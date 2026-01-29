import { useState, useEffect } from "react";
import { motion, AnimatePresence, type Variants } from "framer-motion";
import { ArrowRight } from "lucide-react";
import "./Splash.css";

interface SplashProps {
  onComplete: () => void;
}

const Splash: React.FC<SplashProps> = ({ onComplete }) => {
  const [stage, setStage] = useState(0);

  useEffect(() => {
    // 타이포그래피 시퀀스 관리
    const timers = [
      setTimeout(() => setStage(1), 500),  // Title 등장
      setTimeout(() => setStage(2), 2000), // Description 등장
      setTimeout(() => setStage(3), 3500), // Button 등장
    ];
    return () => timers.forEach(t => clearTimeout(t));
  }, []);

  const titleWords = "StudyLink".split("");
  
  const containerVariants: Variants = {
    hidden: { opacity: 0 },
    visible: { 
      opacity: 1,
      transition: { staggerChildren: 0.1, delayChildren: 0.3 }
    },
    exit: {
      y: -1000,
      opacity: 0,
      transition: { duration: 0.8, ease: [0.76, 0, 0.24, 1] }
    }
  };

  const letterVariants: Variants = {
    hidden: { opacity: 0, y: 50, rotateX: -90, filter: "blur(10px)" },
    visible: { 
      opacity: 1, 
      y: 0, 
      rotateX: 0, 
      filter: "blur(0px)",
      transition: { duration: 0.8, ease: "circOut" }
    }
  };

  const descriptionVariants: Variants = {
    hidden: { opacity: 0, y: 20 },
    visible: { 
      opacity: 1, 
      y: 0,
      transition: { duration: 1, ease: "easeOut" }
    }
  };

  return (
    <motion.div 
      className="splash-screen"
      variants={containerVariants}
      initial="hidden"
      animate="visible"
      exit="exit"
    >
      <div className="splash-bg">
        <div className="glow-orb orb-1" />
        <div className="glow-orb orb-2" />
      </div>

      <div className="splash-content">
        <motion.div className="title-wrapper">
          {titleWords.map((word, idx) => (
            <motion.span
              key={idx}
              variants={letterVariants}
              className="splash-title-char"
            >
              {word}
            </motion.span>
          ))}
        </motion.div>

        <AnimatePresence>
          {stage >= 2 && (
            <motion.p
              variants={descriptionVariants}
              initial="hidden"
              animate="visible"
              className="splash-description"
            >
              연결의 힘이 당신의 학습 가치를 높이는 순간
            </motion.p>
          )}
        </AnimatePresence>

        <AnimatePresence>
          {stage >= 3 && (
            <motion.button
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              onClick={onComplete}
              className="splash-button"
            >
              StudyLink 시작하기
              <ArrowRight className="ml-2 w-5 h-5" />
            </motion.button>
          )}
        </AnimatePresence>
      </div>

      <div className="splash-footer">
        © 2026 StudyLink AI. All rights reserved.
      </div>
    </motion.div>
  );
};

export default Splash;
