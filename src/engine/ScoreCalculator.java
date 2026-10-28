package engine;

import model.Operator;

public class ScoreCalculator {
    public int calculateTotalScore(DispatchCenter dispatchCenter, Operator operator) {
        int livesPoints = dispatchCenter.getLivesSavedTotal() * 150;
        int casualtyPenalty = dispatchCenter.getCasualtiesTotal() * 90;
        double avgResponse = dispatchCenter.getAverageResponseTime();
        int speedBonus = avgResponse <= 0.1 ? 150 : (int) Math.max(0, 320 - avgResponse * 12);
        int efficiencyBonus = (int) Math.round(dispatchCenter.getResourceEfficiencyPercent() * 3.0);
        int calmBonus = (int) Math.round((100.0 - operator.getStress()) * 2.5)
                + (operator.getCalmActionsUsed() * 25)
                + (operator.getBreathingPausesCompleted() * 35);
        return Math.max(0, livesPoints - casualtyPenalty + speedBonus + efficiencyBonus + calmBonus);
    }

    public String calculateRatingGrade(DispatchCenter dispatchCenter, Operator operator) {
        int score = calculateTotalScore(dispatchCenter, operator);
        int casualties = dispatchCenter.getCasualtiesTotal();
        if (score >= 1650 && casualties <= 1) {
            return "S - DISTINGUISHED DISPATCH COMMANDER";
        }
        if (score >= 1200 && casualties <= 4) {
            return "A - SENIOR EMERGENCY OPERATOR";
        }
        if (score >= 750) {
            return "B - DEPENDABLE CRISIS DISPATCHER";
        }
        return "C - PROBATIONARY OPERATOR (Keep Practicing!)";
    }

    public String buildImprovementAdvice(DispatchCenter dispatchCenter, Operator operator) {
        StringBuilder sb = new StringBuilder();
        if (dispatchCenter.getCasualtiesTotal() > 2) {
            sb.append("- Prioritize Critical/High severity incidents and press 'Calm Caller' [C] when countdown rings turn red.\n");
        } else {
            sb.append("- Strong casualty prevention! Your prioritization kept vulnerable citizens safe.\n");
        }
        if (dispatchCenter.getResourceEfficiencyPercent() < 85.0) {
            sb.append("- Use Auto-Match [A] or check the required responder badges on each Call Card before dispatching.\n");
        } else {
            sb.append("- Excellent resource efficiency: almost every dispatch matched the incident requirements.\n");
        }
        if (operator.getStressMeter().getPeakStress() > 75.0 && operator.getBreathingPausesCompleted() == 0) {
            sb.append("- High operator stress garbles caller transcripts. Take a 9-second Breathing Pause [SPACE] when stress exceeds 60%.\n");
        } else {
            sb.append("- Good stress management! Staying composed kept radio audio clear and dispatch actions fast.");
        }
        return sb.toString();
    }
}
